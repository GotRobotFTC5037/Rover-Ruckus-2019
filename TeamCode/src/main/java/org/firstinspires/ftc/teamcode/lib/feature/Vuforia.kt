package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.lib.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot

class Vuforia(parameters: VuforiaLocalizer.Parameters) : Feature {

    val localizer: VuforiaLocalizer = VuforiaLocalizer(parameters)

    class Configuration : VuforiaLocalizer.Parameters(), FeatureConfiguration

    companion object Installer : FeatureInstaller<Configuration, Vuforia> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): Vuforia {
            val parameters = Configuration().apply(configure)
            return Vuforia(parameters)
        }
    }

}