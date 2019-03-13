package org.firstinspires.ftc.teamcode.active.robot

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.control.PIDCoefficients
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.firstinspires.ftc.teamcode.lib.action.ConstantPowerManager
import org.firstinspires.ftc.teamcode.lib.feature.DefaultPowerManager
import org.firstinspires.ftc.teamcode.lib.feature.HeadingCorrection
import org.firstinspires.ftc.teamcode.lib.feature.TargetHeading
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.MecanumDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.opmode.isAutonomous
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.install
import org.firstinspires.ftc.teamcode.lib.robot.robot

object Coda {
    const val FRONT_LEFT_MOTOR = "front left motor"
    const val FRONT_RIGHT_MOTOR = "front right motor"
    const val BACK_LEFT_MOTOR = "back left motor"
    const val BACK_RIGHT_MOTOR = "back right motor"

    const val GEAR_RATIO = 1.0
    const val WHEEL_DIAMETER = 10.16

    const val IMU = "imu"

    const val TRACK_WIDTH = 35.56
    const val WHEEL_BASE = 31.43
    const val TRANSLATIONAL_COEFFICIENTS_P = 0.0
    const val TRANSLATIONAL_COEFFICIENTS_I = 0.0
    const val TRANSLATIONAL_COEFFICIENTS_D = 0.0
    const val HEADING_COEFFICIENTS_P = 0.01
    const val HEADING_COEFFICIENTS_I = 0.0
    const val HEADING_COEFFICIENTS_D = 0.0
    const val FEED_FORWARD_CONSTANT = 0.1
    const val FEED_FORWARD_VELOCITY_GAIN = 1.0
    const val FEED_FORWARD_ACCELERATION_GAIN = 1.0
    const val ADMISSIBLE_ERROR_X = 0.0
    const val ADMISSIBLE_ERROR_Y = 0.0
    const val ADMISSIBLE_ERROR_HEADING = 0.0
    val TIMEOUT = Double.MAX_VALUE

    const val HEADING_CORRECTION_COEFFICIENT = 0.01

    private const val MAXIMUM_TRANSLATIONAL_VELOCITY = 1.0
    private const val MAXIMUM_TRANSLATIONAL_ACCELERATION = 0.1
    private const val MAXIMUM_ANGULAR_VELOCITY = Math.PI / 2
    private const val MAXIMUM_ANGULAR_ACCELERATION = Math.PI / 2

    suspend operator fun invoke(opMode: OpMode): Robot = opMode.Coda()

    fun buildTrajectory(config: TrajectoryBuilder.() -> Unit): Trajectory {
        val builder = TrajectoryBuilder(
            Pose2d(x = 0.0, y = 0.0, heading = 0.0),
            DriveConstraints(
                MAXIMUM_TRANSLATIONAL_VELOCITY,
                MAXIMUM_TRANSLATIONAL_ACCELERATION,
                MAXIMUM_ANGULAR_VELOCITY,
                MAXIMUM_ANGULAR_ACCELERATION
            )
        )
        builder.apply(config)
        return builder.build()
    }
}

@Suppress("FunctionName")
suspend fun OpMode.Coda() = robot {

    install(MecanumDriveTrain) {
        frontLeftMotorName = Coda.FRONT_LEFT_MOTOR
        frontRightMotorName = Coda.FRONT_RIGHT_MOTOR
        backLeftMotorName = Coda.BACK_LEFT_MOTOR
        backRightMotorName = Coda.BACK_RIGHT_MOTOR
    }

    if (isAutonomous) {
        install(MecanumDriveTrain.Localizer) {
            gearRatio = Coda.GEAR_RATIO
            wheelDiameter = Coda.WHEEL_DIAMETER
        }
        install(IMULocalizer) {
            imuName = Coda.IMU
        }
        install(MecanumDriveTrain.RoadRunnerExtension) {
            trackWidth = Coda.TRACK_WIDTH
            wheelBase = Coda.WHEEL_BASE
            translationalCoefficients = PIDCoefficients(
                kP = Coda.TRANSLATIONAL_COEFFICIENTS_P,
                kI = Coda.TRANSLATIONAL_COEFFICIENTS_I,
                kD = Coda.TRANSLATIONAL_COEFFICIENTS_D
            )
            headingCoefficients = PIDCoefficients(
                kP = Coda.HEADING_COEFFICIENTS_P,
                kI = Coda.HEADING_COEFFICIENTS_I,
                kD = Coda.HEADING_COEFFICIENTS_D
            )
            kStatic = Coda.FEED_FORWARD_CONSTANT
            kV = Coda.FEED_FORWARD_VELOCITY_GAIN
            kA = Coda.FEED_FORWARD_ACCELERATION_GAIN
            admissibleError = Pose2d(
                x = Coda.ADMISSIBLE_ERROR_X,
                y = Coda.ADMISSIBLE_ERROR_Y,
                heading = Coda.ADMISSIBLE_ERROR_HEADING
            )
            timeout = Coda.TIMEOUT
        }
        install(TargetHeading) {
            headingLocalizerKey = IMULocalizer
        }
        install(HeadingCorrection) {
            coefficient = Coda.HEADING_CORRECTION_COEFFICIENT
        }
        install(DefaultPowerManager) {
            powerManager = ConstantPowerManager(0.5)
        }
    }

}
