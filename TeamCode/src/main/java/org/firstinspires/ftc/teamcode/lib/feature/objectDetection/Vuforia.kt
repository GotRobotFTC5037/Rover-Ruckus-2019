package org.firstinspires.ftc.teamcode.lib.feature.objectDetection

import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.CoroutineContext

class Vuforia(private val key: String, direction: VuforiaLocalizer.CameraDirection) : Feature {

    val localizer: VuforiaLocalizer by lazy {
        val parameters = VuforiaLocalizer.Parameters().apply {
            vuforiaLicenseKey = key
            cameraDirection = direction
        }
        ClassFactory.getInstance().createVuforia(parameters)
    }

    class Options : FeatureConfiguration {
        var key: String? = null
        var cameraDirection: VuforiaLocalizer.CameraDirection =
            VuforiaLocalizer.CameraDirection.BACK
    }

    companion object Installer : FeatureInstaller<Options, Vuforia> {
        override fun install(
            robot: Robot,
            coroutineContext: CoroutineContext,
            configure: Options.() -> Unit
        ): Vuforia {
            val options = Vuforia.Options().apply(configure)
            val key = options.key ?: throw IllegalStateException("You must provide a key when using a vuforia feature.")
            return Vuforia(key, options.cameraDirection)
        }
    }

}