package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.robot.Robot

class AngleTerminator: Feature {

    class Configuration : FeatureConfiguration

    companion object Installer : FeatureInstaller<Configuration, AngleTerminator> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): AngleTerminator {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}