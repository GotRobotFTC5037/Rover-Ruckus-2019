package org.firstinspires.ftc.teamcode.active.features

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select
import org.corningrobotics.enderbots.endercv.CameraViewDisplay
import org.corningrobotics.enderbots.endercv.OpenCVPipeline
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.opencv.*
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.opencv.core.*
import kotlin.coroutines.CoroutineContext

data class Cargo(
    val gold: List<Rect>,
    val silver: List<Rect>
)

data class CargoRequest(
    val frame: Mat,
    val deferredCargo: CompletableDeferred<Cargo>
)

enum class GoldPosition {
    LEFT, CENTER, RIGHT, UNKNOWN
}

typealias Frame = Mat

interface CargoPositionDetector : Feature {

    val goldPosition: BroadcastChannel<GoldPosition>

    fun enable()
    fun disable()

    companion object Installer : FeatureInstaller<Nothing, CargoPositionDetector> {

        init {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        }

        override fun install(
            robot: Robot,
            coroutineContext: CoroutineContext,
            configure: Nothing.() -> Unit
        ): CargoPositionDetector {
            return CargoPositionDetectorImpl().apply {
                init(robot.hardwareMap.appContext, CameraViewDisplay.getInstance())
            }
        }

    }
}

class CargoPositionDetectorImpl : OpenCVPipeline(),
    CargoPositionDetector, CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val cargoRequestHandler =
        Channel<CargoRequest>(capacity = Channel.UNLIMITED)

    override val goldPosition: BroadcastChannel<GoldPosition> =
        BroadcastChannel(capacity = Channel.CONFLATED)

    override fun onCameraViewStarted(width: Int, height: Int) {
        job = Job()
        startFrameProcessor()
    }

    private fun CoroutineScope.startFrameProcessor(

    ) {
        val frames = Channel<Frame>(capacity = Channel.CONFLATED)
        val cargoPositions = BroadcastChannel<Cargo>(capacity = Channel.CONFLATED)
        frameManager(
            incomingRequests = cargoRequestHandler,
            outgoingFrames = frames,
            incomingCargo = cargoPositions.openSubscription()
        )
        frameWorker(
            incomingFrames = frames,
            outgoingCargo = cargoPositions
        )
        positionProcessor(
            incomingCargo = cargoPositions.openSubscription(),
            outgoingGoldPositions = goldPosition
        )
    }

    override fun processFrame(rgba: Mat?, gray: Mat?): Mat = runBlocking {
        require(rgba != null)

        val deferredCargo = CompletableDeferred<Cargo>()
        val request = CargoRequest(rgba, deferredCargo)
        cargoRequestHandler.send(request)

        val cargo = deferredCargo.await()
        val annotatedFrame: Mat = rgba.clone()
        cargo.gold.forEach { goldBox ->
            val color = RGBColor(212.0, 175.0, 55.0)
            annotatedFrame.addBoundingBoxAndText(goldBox, "Gold", color, 3)
        }
        cargo.silver.forEach { silverBox ->
            val color = RGBColor(192.0, 192.0, 192.0)
            annotatedFrame.addBoundingBoxAndText(silverBox, "Silver", color, 3)
        }
        return@runBlocking annotatedFrame
    }

    private fun CoroutineScope.frameManager(
        incomingRequests: ReceiveChannel<CargoRequest>,
        outgoingFrames: SendChannel<Mat>,
        incomingCargo: ReceiveChannel<Cargo>
    ) = launch {
        var cargo = Cargo(listOf(), listOf())
        while (true) {
            select<Unit> {
                incomingRequests.onReceive { (frame, deferred) ->
                    outgoingFrames.send(frame)
                    deferred.complete(cargo)
                }
                incomingCargo.onReceive { cargo = it }
            }
        }
    }

    private fun CoroutineScope.frameWorker(
        incomingFrames: ReceiveChannel<Frame>,
        outgoingCargo: BroadcastChannel<Cargo>
    ) = launch {
        for (frame in incomingFrames) {
            val originalSize = frame.size()
            val newSize = Size(640.0, 480.0)

            val resizedMat = frame.resized(newSize)
            val blurredMat = resizedMat.boxBlurred(3.0)

            fun List<MatOfPoint>.finalize() =
                this.convexHulls().boundingBoxes().resized(originalSize, newSize)

            val detectedGold = blurredMat
                .hsvThresholding(0.0..120.0, 100.0..255.0, 0.0..255.0)
                .findContours()
                .filtered(minArea = 1250.0)
                .finalize()


            val detectedSilver = blurredMat
                .hsvThresholding(0.0..180.0, 0.0..45.0, 200.0..255.0)
                .eroded(3).dilated(4)
                .findContours()
                .filtered(maxWidth = 100.0, minHeight = 30.0, maxHeight = 100.0)
                .finalize()
                .apply { forEach { it.height *= 2 } }

            val cargo = Cargo(detectedGold, detectedSilver)
            outgoingCargo.send(cargo)
        }
    }

    private fun CoroutineScope.positionProcessor(
        incomingCargo: ReceiveChannel<Cargo>,
        outgoingGoldPositions: BroadcastChannel<GoldPosition>
    ) = launch {
        for ((gold, silver) in incomingCargo) {
            val goldPosition = if (gold.count() == 1 && silver.count() == 2) {
                val goldPosition = gold.first()
                val silver1Position = silver.component1()
                val silver2Position = silver.component2()
                when {
                    goldPosition.x < silver1Position.x && goldPosition.x < silver2Position.x ->
                        GoldPosition.LEFT
                    goldPosition.x > silver1Position.x && goldPosition.x > silver2Position.x ->
                        GoldPosition.RIGHT
                    else -> GoldPosition.CENTER
                }
            } else {
                GoldPosition.UNKNOWN
            }
            outgoingGoldPositions.send(goldPosition)
        }
    }

    override fun onCameraViewStopped() {
        job.cancel()
    }

}
