package org.firstinspires.ftc.teamcode.lib.feature.drivetrain

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.localizer.RobotPositionLocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.hardwareMap
import org.firstinspires.ftc.teamcode.lib.util.sameOrNull
import kotlin.coroutines.CoroutineContext

typealias TankDriveTrainLocalizer = TankDriveTrain.LocalizerInstaller

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
    object LocalizerInstaller : FeatureInstaller<LocalizerConfiguration, PositionLocalizer> {
        override fun install(
            robot: Robot,
            configure: LocalizerConfiguration.() -> Unit
        ): PositionLocalizer {
            val configuration = LocalizerConfiguration().apply(configure)
            val tankDrive = robot[TankDriveTrain]
            val ticksPerRev =
                tankDrive.motors.map { it.motorType.ticksPerRev }.sameOrNull() ?: TODO()
            return tankDrive.PositionLocalizer(
                configuration.wheelDiameter,
                configuration.gearRatio,
                ticksPerRev,
                robot.coroutineContext
            )
        }
    }

    class LocalizerConfiguration : FeatureConfiguration {
        var wheelDiameter = 1.0
        var gearRatio = 1.0
    }

    data class LocalizerUpdate(
        val leftPosition: Double,
        val rightPosition: Double
    ) {
        val average: Double get() = (leftPosition + rightPosition) / 2
    }

    inner class PositionLocalizer(
        wheelDiameter: Double,
        private val gearRatio: Double,
        private val ticksPerRevolution: Double,
        override val coroutineContext: CoroutineContext
    ) : RobotPositionLocalizer, CoroutineScope {

        private val wheelCircumference = wheelDiameter * Math.PI

        private fun DcMotor.currentDistance() =
            currentPosition * wheelCircumference * gearRatio / ticksPerRevolution

        private fun List<DcMotor>.averageDistance(): Double =
            sumByDouble { it.currentDistance() } / count()

        @Suppress("EXPERIMENTAL_API_USAGE")
        fun CoroutineScope.producePosition() = produce(capacity = Channel.CONFLATED) {
            val initialUpdate = LocalizerUpdate(
                leftMotors.averageDistance(),
                rightMotors.averageDistance()
            )
            while (true) {
                val update = LocalizerUpdate(
                    leftMotors.averageDistance() - initialUpdate.leftPosition,
                    rightMotors.averageDistance() - initialUpdate.rightPosition
                )
                send(update)
                yield()
            }
        }

        fun newPositionChannel() = producePosition()

    }

}


