package org.firstinspires.ftc.teamcode.active.production

import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.corningrobotics.enderbots.endercv.OpenCVPipeline
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.opencv.*
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.opencv.core.Core
import org.opencv.core.Mat
import kotlin.coroutines.CoroutineContext

enum class GoldPosition {
    LEFT, CENTER, RIGHT, UNKNOWN
}

class GoldDetector : OpenCVPipeline(), Feature, CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private var originalWidth: Int = 0
    private var originalHeight: Int = 0

    var goldPosition = GoldPosition.UNKNOWN
        private set

    var nGold = 0
        private set

    var nSilver = 0
        private set

    override fun processFrame(rgba: Mat?, gray: Mat?): Mat {
        require(rgba != null)

        val resizedMat = rgba.resized(640, 480)
        val blurredMat = resizedMat.boxBlurred(5.5)

        val detectedGold = blurredMat
            .hsvThresholding(60.0..100.0, 120.0..255.0, 50.0..255.0)
            .dilated(10)
            .findContours()
            .filtered(minArea = 2000.0, maxWidth = 200.0, minWidth = 30.0)
            .convexHulls()
            .boundingBoxes()

        val detectedSilver = blurredMat
            .hsvThresholding(0.0..180.0, 0.0..55.0, 200.0..255.0)
            .dilated(10)
            .findContours()
            .filtered(maxWidth = 100.0, minHeight = 30.0, maxHeight = 100.0)
            .convexHulls()
            .boundingBoxes()

        detectedGold.forEach { blob ->
            val color = RGBColor(212.0, 175.0, 55.0)
            resizedMat.addBoundingBoxAndText(blob, "Gold", color, 3)
        }

        detectedSilver.forEach { contour ->
            val color = RGBColor(192.0, 192.0, 192.0)
            resizedMat.addBoundingBoxAndText(contour, "Silver", color, 3)
        }

        nGold = detectedGold.count()
        nSilver = detectedSilver.count()

        goldPosition = if (detectedGold.count() == 1 && detectedSilver.count() == 2) {
            val goldLocation = detectedGold.component1().x
            val silver1Location = detectedSilver.component1().x
            val silver2Location = detectedSilver.component2().x
            when {
                goldLocation > silver1Location && goldLocation > silver2Location ->
                    GoldPosition.RIGHT

                goldLocation < silver1Location && goldLocation < silver2Location ->
                    GoldPosition.LEFT

                else -> GoldPosition.CENTER
            }
        } else {
            GoldPosition.UNKNOWN
        }

        return resizedMat.resized(originalWidth, originalWidth)
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
