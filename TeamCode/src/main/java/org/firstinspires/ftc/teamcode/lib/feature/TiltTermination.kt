package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.robot.Robot

class TiltTermination: Feature {

    class Configuration : FeatureConfiguration

    companion object Installer : FeatureInstaller<Configuration, TiltTermination> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): TiltTermination {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}