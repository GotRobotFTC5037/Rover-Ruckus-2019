package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot

class Intake(
    private val intakeLiftMotor: DcMotor,
    private val intakeMotor: DcMotor
) : Feature {

    fun setLiftPower(power: Double) {
        intakeLiftMotor.power = power
    }

    fun setIntakePower(power: Double) {
        intakeMotor.power = power
    }

    class Configuration : FeatureConfiguration {
        var intakeLift = "intake lift"
        var intake = "intake"
    }

    companion object Installer : FeatureInstaller<Configuration, Intake> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): Intake {
            val configuration = Configuration().apply(configure)
            val lift = robot.linearOpMode.hardwareMap.get(DcMotor::class.java, configuration.intakeLift)
            val intake = robot.linearOpMode.hardwareMap.get(DcMotor::class.java, configuration.intake)
            return Intake(lift, intake)
        }
    }
}