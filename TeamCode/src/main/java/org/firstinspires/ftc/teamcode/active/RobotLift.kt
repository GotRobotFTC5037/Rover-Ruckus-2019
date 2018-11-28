@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.TouchSensor
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot

const val LIFT_DOWN_POSITION = 26_500

class RobotLift(
    private val liftMotor: DcMotor,
    private val liftButton: TouchSensor
) : Feature {

    val liftPosition: Int get() = liftMotor.currentPosition

    fun setPower(power: Double) {
        if (power < 0.0 && !liftButton.isPressed) {
            liftMotor.power = power
        } else if (power >= 0.0) {
            liftMotor.power = power
        }
    }

    suspend fun retract() {
        liftMotor.power = -1.0
        while (!liftButton.isPressed) {
            yield()
        }
        liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        liftMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        liftMotor.power = 0.0
    }

    suspend fun extend() {
        liftMotor.power = 1.0
        while (liftPosition < LIFT_DOWN_POSITION) {
            yield()
        }
        liftMotor.power = 0.0
    }

    class Configuration : FeatureConfiguration {
        var liftMotorName: String = "lift motor"
        var liftButton: String = "lift button"
    }

    companion object Installer : FeatureInstaller<Configuration, RobotLift> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): RobotLift {
            val config = Configuration().apply(configure)

            val liftMotor = robot.hardwareMap.get(DcMotor::class.java, config.liftMotorName)
            liftMotor.direction = DcMotorSimple.Direction.REVERSE
            liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            liftMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

            val liftButton = robot.hardwareMap.get(TouchSensor::class.java, config.liftButton)

            return RobotLift(liftMotor, liftButton)
        }
    }
}