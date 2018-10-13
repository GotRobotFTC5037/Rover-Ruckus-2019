package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce

class InvalidDriveTrainOperationException(override val message: String? = null) : Exception()

class RobotTankDriveTrain(
    private val leftMotors: List<DcMotor>,
    private val rightMotors: List<DcMotor>
) : RobotDriveTrain {

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

    inner class Localizer(private val coroutineScope: CoroutineScope) : RobotPositionLocalizer {

        private fun CoroutineScope.motorPositionChannel() = produce<Int> {
            val motors = leftMotors + rightMotors
            val currentPosition = motors.sumBy { it.currentPosition }
            offer(currentPosition / motors.count())
        }

        private fun CoroutineScope.positionChannel(motorPositionChannel: ReceiveChannel<Int>) =
            produce<RobotPosition> {
                offer(RobotPosition(motorPositionChannel.receive().toDouble(), 0.0))
            }

        override fun newPositionChannel(): ReceiveChannel<RobotPosition> {
            (leftMotors + rightMotors).forEach {
                it.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
                it.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            }
            val motorPosition = coroutineScope.motorPositionChannel()
            return coroutineScope.positionChannel(motorPosition)

        }
    }

    object PositionLocalizer : RobotFeature<Nothing, Localizer> {
        override val key: RobotFeatureKey<Localizer> =
            RobotFeatureKey("RobotTankDriveLocalizer")

        override fun install(robot: Robot, configure: Nothing.() -> Unit): Localizer {
            return robot.feature(RobotTankDriveTrain).Localizer(robot as CoroutineScope)
        }
    }

    companion object Feature : RobotFeature<Configuration, RobotTankDriveTrain> {

        override val key = RobotFeatureKey<RobotTankDriveTrain>("RobotTankDriveTrain")

        override fun install(
            robot: Robot,
            configure: Configuration.() -> Unit
        ): RobotTankDriveTrain {
            val configuration = Configuration(robot.hardwareMap).apply(configure)
            return RobotTankDriveTrain(configuration.leftMotors, configuration.rightMotors)
        }

    }

    @RobotFeatureMarker
    class Configuration(val hardwareMap: HardwareMap) : RobotFeatureConfiguration {

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

}

