package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.robot.Robot

class InterferenceDetection : Feature {

    companion object Installer : FeatureInstaller<Configuration, InterferenceDetection> {
        override fun install(
            robot: Robot,
            configure: Configuration.() -> Unit
        ): InterferenceDetection {
            TODO("not implemented")
        }
    }

    class Configuration : FeatureConfiguration
}
