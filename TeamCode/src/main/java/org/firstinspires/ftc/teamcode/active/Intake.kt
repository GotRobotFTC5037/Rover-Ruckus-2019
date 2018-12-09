package org.firstinspires.ftc.teamcode.active

import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot

class Intake : Feature{
    class Configuration : FeatureConfiguration{

    }
    companion object Installer : FeatureInstaller<Configuration,Intake>{
        override fun install(robot: Robot, configure: Configuration.() -> Unit): Intake {
            val configuration = Configuration().apply(configure)
            val intake = Intake()
            return intake
        }
    }
}