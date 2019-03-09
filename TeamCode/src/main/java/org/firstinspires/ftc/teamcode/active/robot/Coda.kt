package org.firstinspires.ftc.teamcode.active.robot

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.firstinspires.ftc.teamcode.lib.action.ConstantPowerManager
import org.firstinspires.ftc.teamcode.lib.feature.DefaultPowerManager
import org.firstinspires.ftc.teamcode.lib.feature.HeadingCorrection
import org.firstinspires.ftc.teamcode.lib.feature.TargetHeading
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.MecanumDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.install
import org.firstinspires.ftc.teamcode.lib.robot.robot

object CodaConstants {
    const val FRONT_LEFT_MOTOR = "front left motor"
    const val FRONT_RIGHT_MOTOR = "front right motor"
    const val BACK_LEFT_MOTOR = "back left motor"
    const val BACK_RIGHT_MOTOR = "back right motor"
    const val IMU = "imu"
    const val HEADING_CORRECTION_COEFFICIENT = 0.0
}

@Suppress("FunctionName")
suspend fun OpMode.Coda() = robot {
    install(MecanumDriveTrain) {
        frontLeftMotorName = CodaConstants.FRONT_LEFT_MOTOR
        frontRightMotorName = CodaConstants.FRONT_RIGHT_MOTOR
        backLeftMotorName = CodaConstants.BACK_LEFT_MOTOR
        backRightMotorName = CodaConstants.BACK_RIGHT_MOTOR
    }
    install(IMULocalizer) {
        imuName = CodaConstants.IMU
    }
    install(TargetHeading) {
        headingLocalizerKey = IMULocalizer
    }
    install(HeadingCorrection) {
        coefficient = CodaConstants.HEADING_CORRECTION_COEFFICIENT
    }
    install(DefaultPowerManager) {
        powerManager = ConstantPowerManager(0.5)
    }
}