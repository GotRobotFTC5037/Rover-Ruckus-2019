package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.CoroutineScope
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.teamcode.active.features.CargoDetector
import org.firstinspires.ftc.teamcode.active.features.Intake
import org.firstinspires.ftc.teamcode.active.features.Lift
import org.firstinspires.ftc.teamcode.active.features.MarkerDeployer
import org.firstinspires.ftc.teamcode.lib.ConstantPowerManager
import org.firstinspires.ftc.teamcode.lib.NothingPowerManager
import org.firstinspires.ftc.teamcode.lib.action.Drive
import org.firstinspires.ftc.teamcode.lib.action.TurnTo
import org.firstinspires.ftc.teamcode.lib.action.UnspecifiedMoveActionType
import org.firstinspires.ftc.teamcode.lib.feature.DefaultPowerManager
import org.firstinspires.ftc.teamcode.lib.feature.HeadingCorrection
import org.firstinspires.ftc.teamcode.lib.feature.TargetHeading
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.MotorDirection
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrainLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.feature.vision.Vuforia
import org.firstinspires.ftc.teamcode.lib.robot.install
import org.firstinspires.ftc.teamcode.lib.robot.robot
import org.firstinspires.ftc.teamcode.lib.util.isAutonomous

@Suppress("SpellCheckingInspection")
object RobotConstants {

    const val LEFT_DRIVE_MOTOR = "left motor"
    const val RIGHT_DRIVE_MOTOR = "right motor"
    const val LIFT_MOTOR = "lift motor"
    const val MARKER_DEPLOYER_SERVO = "marker"
    const val INTAKE_LIFT_MOTOR = "intake lift"
    const val INTAKE_MOTOR = "intake"
    const val IMU = "imu"
    const val WEBCAM = "webcam"
    const val WHEEL_DIAMETER = 10.16
    const val CARGO_DETECTION_MIN_CONFIDENCE = 0.45

    // TODO: Put this key into a file and read from the file.
    const val VUFORIA_KEY = "Af8tA0P/////AAABmS0VzHrieUymkaB0I3Xxz04Khxz8ayagqgOyxunzdcpieUETaApI" +
            "5AxHTeLxmZEhFE6MGDV42Z86mWVaGGAW2Ust+8e9Cz8oEbz7yj+zbjydNHI8b+5ShaGaR9/CEeEytBl7MBg8" +
            "viMLEnNO0imxu/2GN4wyIWUgTkEL0H2yehJnwwQvLwyT3Q/F/8Evips+fjLuu8kvfjmuyBobIJYR2Xk29aXv" +
            "fXlizL8YHq8IlddJw90weHjjvi5BMOszJKGbZwb3H5luPl0KKW6EvzMpQWblHS+OQkXWNhr9lMjpuxncDKYq" +
            "BtuNm+aXd67bMKHPu4AJ9HmC7b4hj57Jx7xB3IF+pXq8T0NkjVLzc89W1Xf+"
}

suspend fun roverRuckusRobot(
    linearOpMode: LinearOpMode,
    coroutineScope: CoroutineScope,
    shouldUseCamera: Boolean = true
) = robot(linearOpMode, coroutineScope) {

        // Components
        install(TankDriveTrain) {
            addLeftMotor(RobotConstants.LEFT_DRIVE_MOTOR, MotorDirection.FORWARD)
            addRightMotor(RobotConstants.RIGHT_DRIVE_MOTOR, MotorDirection.REVERSE)
        }
        install(Lift) {
            liftMotorName = RobotConstants.LIFT_MOTOR
        }
        install(MarkerDeployer) {
            servoName = RobotConstants.MARKER_DEPLOYER_SERVO
        }
        install(Intake) {
            intakeLift = RobotConstants.INTAKE_LIFT_MOTOR
            intake = RobotConstants.INTAKE_MOTOR
        }

        // Localizer
        install(IMULocalizer) {
            imuName = RobotConstants.IMU
            order = AxesOrder.ZYX
            initialHeading = 90.0
        }

        // Autonomous
        if (linearOpMode.isAutonomous()) {

            // Localizer
            install(TankDriveTrainLocalizer) {
                wheelDiameter = RobotConstants.WHEEL_DIAMETER
            }

            // Vision
            if (shouldUseCamera) {
                install(Vuforia) {
                    vuforiaLicenseKey = RobotConstants.VUFORIA_KEY
                    fillCameraMonitorViewParent = true
                    cameraName = linearOpMode.hardwareMap.get(
                        WebcamName::class.java,
                        RobotConstants.WEBCAM
                    )
                }
                install(CargoDetector) {
                    minimumConfidence = RobotConstants.CARGO_DETECTION_MIN_CONFIDENCE
                    useObjectTracker = true
                }
            }

            // Drive Correction
            install(TargetHeading) {
                initialTargetHeading = 90.0
            }
            install(HeadingCorrection) {
                coefficient = 0.15
            }

            // Pipeline Interceptors
            install(DefaultPowerManager) {
                UnspecifiedMoveActionType uses NothingPowerManager
                Drive uses ConstantPowerManager(power = 0.75)
                TurnTo uses ConstantPowerManager(power = 0.75)
            }

        }
    }

