package org.firstinspires.ftc.teamcode.lib.feature.objectDetection

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.lib.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.CoroutineContext

class Vuforia(private val key: String, direction: VuforiaLocalizer.CameraDirection) : Feature {

    val localizer: VuforiaLocalizer

    init {
        val parameters = VuforiaLocalizer.Parameters().apply {
            vuforiaLicenseKey = key
            cameraDirection = direction
        }
        localizer = VuforiaLocalizer(parameters)
    }

    class Configuration : FeatureConfiguration {
        var key: String? = null
        var cameraDirection: VuforiaLocalizer.CameraDirection =
            VuforiaLocalizer.CameraDirection.BACK
        var webcamName: String? = null
    }

    companion object Installer : FeatureInstaller<Configuration, Vuforia> {
        override fun install(
            robot: Robot,
            coroutineContext: CoroutineContext,
            configure: Configuration.() -> Unit
        ): Vuforia {
            val options = Vuforia.Configuration().apply(configure)
            val key = options.key
                ?: throw IllegalStateException("You must provide a key when using a Vuforia.")
            return Vuforia(key, options.cameraDirection)
        }
    }

}