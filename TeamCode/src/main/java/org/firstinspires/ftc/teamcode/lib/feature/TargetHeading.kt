package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.robot.Robot

class TargetHeading(
    private val initalTargetHeading: Double
): Feature {

    class Configuration: FeatureConfiguration {

    }

    companion object Installer : FeatureInstaller<Configuration, TargetHeading> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): TargetHeading {
            val configuration = Configuration().apply(configure)
            return TargetHeading(0.0)
        }
    }
}