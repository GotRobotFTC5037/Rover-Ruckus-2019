package org.firstinspires.ftc.teamcode.active.production

import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import org.corningrobotics.enderbots.endercv.OpenCVPipeline
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.opencv.*
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.opencv.core.Core
import org.opencv.core.KeyPoint
import org.opencv.core.Mat
import kotlin.coroutines.CoroutineContext

enum class GoldPosition {
    LEFT, CENTER, RIGHT, UNKNOWN
}

class GoldDetector : OpenCVPipeline(), Feature, CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = newSingleThreadContext("GoldDetector") + job

    private var originalWidth: Int = 0
    private var originalHeight: Int = 0

    private fun CoroutineScope.processFrame(frame: Mat): Deferred<Mat> = async {
        val resizedMat = frame.resized(640, 480)
        val blurredMat = resizedMat.boxBlurred(5.5)
        val detectedGold = blurredMat
            .hsvThresholding(0.0..100.0, 120.0..255.0, 0.0..255.0)
            .dilated(10)
            .findBlobs()
        val detectedSilver = blurredMat
            .hsvThresholding(0.0..180.0, 0.0..55.0, 200.0..255.0)
            .findContours()
            .filtered(maxWidth = 100.0, minHeight = 30.0, maxHeight = 100.0)
            .convexHulls()
            .boundingBoxes()

//        if (detectedGold.count() == 1 && detectedSilver.count() == 2) {
//            val goldXPosition = detectedGold.first().pt.x
//            val silver1XPosition = detectedSilver.component1().x
//            val silver2XPosition = detectedSilver.component2().x
//            val goldPosition = when {
//                goldXPosition > silver1XPosition && goldXPosition > silver2XPosition ->
//                    GoldPosition.RIGHT
//                goldXPosition < silver1XPosition && goldXPosition < silver2XPosition ->
//                    GoldPosition.LEFT
//                else -> GoldPosition.CENTER
//            }
//            goldPositionChannel.send(goldPosition)
//        } else {
//            goldPositionChannel.send(GoldPosition.UNKNOWN)
//        }

        detectedGold.forEach { blob ->
            val color = RGBColor(212.0, 175.0, 55.0)
            resizedMat.addBoundingBoxAndText(blob, "Gold", color, 3)
        }
        detectedSilver.forEach { contour ->
            val color = RGBColor(192.0, 192.0, 192.0)
            resizedMat.addBoundingBoxAndText(contour, "Silver", color, 3)
        }
        resizedMat.resized(originalWidth, originalWidth)
    }

    private fun CoroutineScope.startFrameProcessor(
        incomingFrames: ReceiveChannel<Mat>,
        processedFrames: SendChannel<Mat>,
        goldPosition: BroadcastChannel<GoldPosition>
    ) = launch {

        var detectedGold = listOf<KeyPoint>()
        var detectedSilver = listOf<KeyPoint>()

        while (true) {
            val incomingFrame = incomingFrames.receive()

        }
    }

    override fun processFrame(rgba: Mat?, gray: Mat?): Mat = runBlocking {
        require(rgba != null)
        processFrame(rgba).await()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        originalWidth = width
        originalHeight = height

        job = Job()
    }

    override fun onCameraViewStopped() {
        job.cancel()
    }

    companion object Installer : FeatureInstaller<Nothing, GoldDetector> {

        init {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        }

        override fun install(
            robot: Robot,
            hardwareMap: HardwareMap,
            coroutineContext: CoroutineContext,
            configure: Nothing.() -> Unit
        ): GoldDetector {
            return GoldDetector()
        }

    }

}
