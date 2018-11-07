@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.lib.feature.drivetrain

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.isActive
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.localizer.Position
import org.firstinspires.ftc.teamcode.lib.feature.localizer.RobotPositionLocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.CoroutineContext

class TankDriveTrain(
    private val leftMotors: List<DcMotor>,
    private val rightMotors: List<DcMotor>
) : DriveTrain {

    private val motors get() = leftMotors + rightMotors

    override fun setHeadingPower(power: Double) {
        setMotorPowers(-power, power)
    }

    override fun setPower(linearPower: Double, lateralPower: Double) {
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

    inner class PositionLocalizer(
        override val coroutineContext: CoroutineContext,
        wheelDiameter: Double,
        private val ticksPerRevolution: Int
    ) : RobotPositionLocalizer, CoroutineScope {

        override val isReady: Boolean = true

        private val wheelCircumference = wheelDiameter * Math.PI

        private fun CoroutineScope.producePosition(
            ticker: ReceiveChannel<Unit>
        ): BroadcastChannel<Position> =
            broadcast(capacity = Channel.CONFLATED) {
                while (isActive) {
                    ticker.receive()
                    val motors = this@TankDriveTrain.motors
                    val encoderTicks = motors.sumBy { it.currentPosition } / motors.count()
                    val linearPosition = (encoderTicks / ticksPerRevolution) * wheelCircumference
                    val position = Position(linearPosition, 0.0)
                    send(position)
                }
            }

        override val position: BroadcastChannel<Position> =
            producePosition(ticker(10, mode = TickerMode.FIXED_DELAY))

    }

    class PositionLocalizerConfiguration : FeatureConfiguration {
        var ticksPerRevolution = 360
        var wheelDiameter = ticksPerRevolution / Math.PI
    }

    object Localizer : FeatureInstaller<PositionLocalizerConfiguration, PositionLocalizer> {
        override fun install(
            robot: Robot, configure: PositionLocalizerConfiguration.() -> Unit
        ): PositionLocalizer {
            val configuration = PositionLocalizerConfiguration().apply(configure)
            val tankDrive = robot[TankDriveTrain]
            return tankDrive.PositionLocalizer(
                robot.coroutineContext,
                configuration.wheelDiameter,
                configuration.ticksPerRevolution
            )
        }
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
        override fun install(robot: Robot, configure: Configuration.() -> Unit): TankDriveTrain {
            val configuration = Configuration(robot.hardwareMap).apply(configure)
            return TankDriveTrain(configuration.leftMotors, configuration.rightMotors)
        }
    }

}


