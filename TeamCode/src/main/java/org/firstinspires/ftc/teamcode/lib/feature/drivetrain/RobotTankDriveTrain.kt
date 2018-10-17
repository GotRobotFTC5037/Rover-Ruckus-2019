package org.firstinspires.ftc.teamcode.lib.feature.drivetrain

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.TickerMode
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.channels.ticker
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.localizer.RobotPosition
import org.firstinspires.ftc.teamcode.lib.feature.localizer.RobotPositionLocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot

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

    class Configuration(private val hardwareMap: HardwareMap) : RobotFeatureConfiguration {

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

    companion object FeatureInstaller : RobotFeatureInstaller<Configuration, RobotTankDriveTrain> {

        override fun install(
                robot: Robot,
                configure: Configuration.() -> Unit
        ): RobotTankDriveTrain {
            val configuration = Configuration(robot.hardwareMap).apply(configure)
            return RobotTankDriveTrain(configuration.leftMotors, configuration.rightMotors)
        }

    }

    class LocalizerConfiguration : RobotFeatureConfiguration {
        var wheelDiameter: Double = 1440 / Math.PI
    }

    inner class Localizer(
            private val coroutineScope: CoroutineScope,
            wheelDiameter: Double
    ) : RobotPositionLocalizer {

        private val wheelPositionMultiplier = Math.PI * wheelDiameter / 1440

        override val isReady: Boolean = true

        private val motors
            get() = this@RobotTankDriveTrain.leftMotors + this@RobotTankDriveTrain.rightMotors

        override val positionChannel: ReceiveChannel<RobotPosition> =
                coroutineScope.newPositionChannel(
                        coroutineScope.motorPositionChannel(
                                ticker(10, mode = TickerMode.FIXED_PERIOD)
                        )
                )

        private fun CoroutineScope.motorPositionChannel(ticker: ReceiveChannel<Unit>) =
                produce<Int> {
                    ticker.receive()
                    val currentPosition = motors.sumBy { it.currentPosition }
                    offer(currentPosition / motors.count())
                }

        private fun CoroutineScope.newPositionChannel(motorPositionChannel: ReceiveChannel<Int>) =
                produce<RobotPosition> {
                    val position = motorPositionChannel.receive() * wheelPositionMultiplier
                    offer(RobotPosition(position, 0.0))
                }
    }

    object PositionLocalizer : RobotFeatureInstaller<LocalizerConfiguration, Localizer> {

        override fun install(
                robot: Robot,
                configure: LocalizerConfiguration.() -> Unit
        ): Localizer {
            val configuration = LocalizerConfiguration().apply(configure)
            val driveTrain = robot.feature(RobotTankDriveTrain::class)
            return driveTrain.Localizer(robot as CoroutineScope, configuration.wheelDiameter)
        }

    }

}

