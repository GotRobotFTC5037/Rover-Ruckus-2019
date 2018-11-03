package org.firstinspires.ftc.teamcode.active.features

import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot

class LanderLatch(private val liftMotor: DcMotor) : Feature {

    suspend fun raiseRobot() {

    }

    suspend fun lowerRobot() {

    }

    class Configuration : FeatureConfiguration {
        var liftMotorName: String = "lift"
    }

    companion object Installer : FeatureInstaller<Configuration, LanderLatch> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): LanderLatch {
            val configuration = Configuration().apply(configure)
            val liftMotor = robot.hardwareMap.get(DcMotor::class.java, configuration.liftMotorName)
            return LanderLatch(liftMotor)
        }
    }
}