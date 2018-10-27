package org.firstinspires.ftc.teamcode.active.production

import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select
import org.corningrobotics.enderbots.endercv.OpenCVPipeline
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.opencv.*
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.opencv.core.Core
import org.opencv.core.Mat
import kotlin.coroutines.CoroutineContext

class GoldDetector : OpenCVPipeline(), Feature, CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = newSingleThreadContext("GoldDetector") + job

    private var originalWidth: Int = 0
    private var originalHeight: Int = 0

    private lateinit var processFrameChannel: SendChannel<Mat>
    private lateinit var annotatedFrameChannel: ReceiveChannel<Mat>

    private fun CoroutineScope.startFrameProcessor(
        incomingFrames: ReceiveChannel<Mat>,
        processedFrames: SendChannel<Mat>
    ) = launch {
        while (true) {
            select<Unit> {
                incomingFrames.onReceive { incomingFrame ->
                    val resizedMat = incomingFrame.resized(640, 480)
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
                    detectedGold.forEach { blob ->
                        val color = RGBColor(212.0, 175.0, 55.0)
                        incomingFrame.addBoundingBoxAndText(blob, "Gold", color, 3)
                    }
                    detectedSilver.forEach { contour ->
                        val color = RGBColor(192.0, 192.0, 192.0)
                        incomingFrame.addBoundingBoxAndText(contour, "Silver", color, 3)
                    }
                    incomingFrame.resized(originalWidth, originalWidth)
                    processedFrames.send(incomingFrame)
                }
            }
        }

    }

    override fun processFrame(rgba: Mat?, gray: Mat?): Mat = runBlocking {
        require(rgba != null)
        processFrameChannel.send(rgba)
        annotatedFrameChannel.receive()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        originalWidth = width
        originalHeight = height

        job = Job()

        val processFrameChannel = Channel<Mat>()
        val annotatedFrameChannel = Channel<Mat>()
        startFrameProcessor(processFrameChannel, annotatedFrameChannel)
        this.processFrameChannel = processFrameChannel
        this.annotatedFrameChannel = annotatedFrameChannel
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
