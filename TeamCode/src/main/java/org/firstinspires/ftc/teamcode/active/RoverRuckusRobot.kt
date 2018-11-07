package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.feature.objectDetection.Vuforia
import org.firstinspires.ftc.teamcode.lib.robot.robot

fun roverRuckusRobot(linearOpMode: LinearOpMode) = robot(linearOpMode) {
    install(TankDriveTrain) {
        addLeftMotor("left motor")
        addRightMotor("right motor")
    }
    install(RobotLift) {
        liftMotorName = "lift motor"
    }
    install(TankDriveTrain.Localizer) {
        ticksPerRevolution = 360
        wheelDiameter = ticksPerRevolution / Math.PI
    }
    install(IMULocalizer) {
        imuName = "imu"
    }
    install(Vuforia) {
        vuforiaLicenseKey = RobotConstants.VUFORIA_KEY
        fillCameraMonitorViewParent = true
    }
    install(CargoDetector) {
        minimumConfidence = RobotConstants.CARGO_DETECTION_MIN_CONFIDENCE
        useObjectTracker = true
    }
}
