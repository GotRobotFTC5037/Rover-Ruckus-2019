package org.firstinspires.ftc.teamcode.lib.feature

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.isActive
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.hardwareMap
import kotlin.coroutines.CoroutineContext

class TankDriveTrain(
    private val leftMotors: List<DcMotor>,
    private val rightMotors: List<DcMotor>,
    override val coroutineContext: CoroutineContext
) : DriveTrain, CoroutineScope {

    private val motors get() = leftMotors + rightMotors

    fun setMotorPowers(leftPower: Double, rightPower: Double) {
        fun setMotorPowers(power: Double, motors: List<DcMotor>) {
            for (motor in motors) {
                motor.power = power
            }
        }
        setMotorPowers(leftPower, leftMotors)
        setMotorPowers(rightPower, rightMotors)
    }

    override fun stop() {
        setMotorPowers(0.0, 0.0)
    }

    companion object Installer : FeatureInstaller<Configuration, TankDriveTrain> {

        override fun install(robot: Robot, configure: Configuration.() -> Unit): TankDriveTrain {
            val configuration = Configuration(
                robot.hardwareMap
            ).apply(configure)
            return TankDriveTrain(
                configuration.leftMotors,
                configuration.rightMotors,
                robot.coroutineContext
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

    /**
     * The [PositionLocalizer] for localizing a [TankDriveTrain].
     */
    object Localizer : FeatureInstaller<PositionLocalizerConfiguration, PositionLocalizer> {
        override fun install(
            robot: Robot, configure: PositionLocalizerConfiguration.() -> Unit
        ): PositionLocalizer {
            val configuration = PositionLocalizerConfiguration().apply(configure)
            val tankDrive = robot[TankDriveTrain]
            return tankDrive.PositionLocalizer(
                robot.coroutineContext,
                configuration.wheelDiameter,
                configuration.gearRatio
            )
        }
    }

    class PositionLocalizerConfiguration : FeatureConfiguration {
        var wheelDiameter = 1.0
        var gearRatio = 1.0
    }

    inner class PositionLocalizer(
        override val coroutineContext: CoroutineContext,
        wheelDiameter: Double,
        private val gearRatio: Double
    ) : RobotPositionLocalizer, CoroutineScope {

        override val isReady: Boolean = true

        private val wheelCircumference = wheelDiameter * Math.PI

        private val ticksPerRevolution = motors.first().motorType.ticksPerRev

        private fun DcMotor.currentDistance() =
            currentPosition * wheelCircumference * gearRatio / ticksPerRevolution

        private fun List<DcMotor>.averageDistance(): Double =
            sumByDouble { it.currentDistance() } / count()

        private fun CoroutineScope.producePosition(
            ticker: ReceiveChannel<Unit>
        ): BroadcastChannel<Position> =
            broadcast(capacity = Channel.CONFLATED) {
                while (isActive) {
                    ticker.receive()
                    val position = Position(motors.averageDistance(), 0.0)
                    send(position)
                }
            }

        override val position: BroadcastChannel<Position> =
            producePosition(ticker(10, mode = TickerMode.FIXED_DELAY))

    }

}


