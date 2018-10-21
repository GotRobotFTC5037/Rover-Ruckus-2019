package org.firstinspires.ftc.teamcode.lib.feature.drivetrain

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import kotlin.coroutines.experimental.CoroutineContext

class TankDriveTrain(
    private val leftMotors: List<DcMotor>,
    private val rightMotors: List<DcMotor>
) : DriveTrain {

    override fun setHeadingPower(power: Double) {
        setMotorPowers(-power, power)
    }

    override fun setPower(linearPower: Double, lateralPower: Double) {
        // TODO: Add a message to the exception.
        if (lateralPower != 0.0) throw InvalidDriveTrainOperationException()
        setMotorPowers(linearPower, linearPower)
    }

    fun setMotorPowers(leftPower: Double, rightPower: Double) {
        fun setMotorPowers(power: Double, motors: List<DcMotor>) {
            for (motor in motors) {
                motor.power = power
            }
        }
        setMotorPowers(leftPower, leftMotors)
        setMotorPowers(rightPower, rightMotors)
    }

    override fun stopAllMotors() {
        setMotorPowers(0.0, 0.0)
    }

    class Configuration(private val hardwareMap: HardwareMap) : FeatureConfiguration {

        val leftMotors: MutableList<DcMotor> = mutableListOf()
        val rightMotors: MutableList<DcMotor> = mutableListOf()

        fun addLeftMotor(name: String, direction: MotorDirection = MotorDirection.REVERSE) {
            val motor = hardwareMap.get(DcMotor::class.java, name)
            motor.direction = direction
            leftMotors.add(motor)
        }

        fun addRightMotor(name: String, direction: MotorDirection = MotorDirection.FORWARD) {
            val motor = hardwareMap.get(DcMotor::class.java, name)
            motor.direction = direction
            rightMotors.add(motor)
        }

    }

    companion object Installer : FeatureInstaller<Configuration, TankDriveTrain> {
        override fun install(
            hardwareMap: HardwareMap,
            coroutineContext: CoroutineContext,
            configure: Configuration.() -> Unit
        ): TankDriveTrain {
            val configuration = Configuration(hardwareMap).apply(configure)
            return TankDriveTrain(configuration.leftMotors, configuration.rightMotors)
        }
    }

}

