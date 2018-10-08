package us.gotrobot.grbase

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap

typealias TankDrive = RobotTankDriveTrain
typealias TankDriveConfiguration = RobotTankDriveTrain.Configuration

class RobotTankDriveTrain(
    private val leftMotors: List<DcMotor>,
    private val rightMotors: List<DcMotor>
) : RobotDriveTrain {

    fun setMotorPowers(leftPower: Double, rightPower: Double) {
        fun setMotorPowers(power: Double, motors: List<DcMotor>) {
            for (motor in motors) {
                motor.power = power
            }
        }
        setMotorPowers(leftPower, leftMotors)
        setMotorPowers(rightPower, rightMotors)
    }

    class Configuration(override val hardwareMap: HardwareMap) : ComponentConfiguration {

        val leftMotors = mutableListOf<DcMotor>()
        val rightMotors = mutableListOf<DcMotor>()

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

    companion object Installer : ComponentInstaller<TankDriveConfiguration, TankDrive> {

        override val key = RobotComponentInstallerKey<TankDrive>("RobotTankDriveTrain")

        override fun install(
            robot: Robot,
            configure: TankDriveConfiguration.() -> Unit
        ): TankDrive {
            val configuration = Configuration(robot.hardwareMap).apply(configure)
            return TankDrive(configuration.leftMotors, configuration.rightMotors)
        }

    }

}
