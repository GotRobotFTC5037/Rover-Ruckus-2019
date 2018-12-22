package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.robot.Robot

class HeadingCorrection : Feature {

    companion object Installer : FeatureInstaller<Configuration, HeadingCorrection> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): HeadingCorrection {
            TODO("not implemented")
        }
    }

    class Configuration : FeatureConfiguration
}