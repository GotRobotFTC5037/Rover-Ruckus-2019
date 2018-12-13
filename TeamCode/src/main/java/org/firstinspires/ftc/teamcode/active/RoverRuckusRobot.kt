package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.CoroutineScope
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.teamcode.lib.action.MoveActionType
import org.firstinspires.ftc.teamcode.lib.feature.*
import org.firstinspires.ftc.teamcode.lib.util.isAutonomous
import org.firstinspires.ftc.teamcode.lib.robot.robot
import org.firstinspires.ftc.teamcode.lib.ConstantPowerManager

object RobotConstants {

    @Suppress("SpellCheckingInspection")
    const val VUFORIA_KEY = "Af8tA0P/////AAABmS0VzHrieUymkaB0I3Xxz04Khxz8ayagqgOyxunzdcpieUETaApI" +
            "5AxHTeLxmZEhFE6MGDV42Z86mWVaGGAW2Ust+8e9Cz8oEbz7yj+zbjydNHI8b+5ShaGaR9/CEeEytBl7MBg8" +
            "viMLEnNO0imxu/2GN4wyIWUgTkEL0H2yehJnwwQvLwyT3Q/F/8Evips+fjLuu8kvfjmuyBobIJYR2Xk29aXv" +
            "fXlizL8YHq8IlddJw90weHjjvi5BMOszJKGbZwb3H5luPl0KKW6EvzMpQWblHS+OQkXWNhr9lMjpuxncDKYq" +
            "BtuNm+aXd67bMKHPu4AJ9HmC7b4hj57Jx7xB3IF+pXq8T0NkjVLzc89W1Xf+"

    const val CARGO_DETECTION_MIN_CONFIDENCE = 0.45
}

suspend fun roverRuckusRobot(linearOpMode: LinearOpMode, coroutineScope: CoroutineScope) =
    robot(linearOpMode, coroutineScope) {
        install(TankDriveTrain) {
            addLeftMotor("left motor", MotorDirection.FORWARD)
            addRightMotor("right motor", MotorDirection.REVERSE)
        }
        install(RobotLift) {
            liftMotorName = "lift motor"
        }
        install(MarkerDeployer) {
            servoName = "marker"
        }
        install(TankDriveTrain.Localizer) {
            wheelDiameter = 10.16
        }
        install(IMULocalizer) {
            imuName = "imu"
        }
        install(Intake) {
            intakeLift = "intake lift"
            intake = "intake"
        }
        if (linearOpMode.isAutonomous()) {
            install(Vuforia) {
                vuforiaLicenseKey = RobotConstants.VUFORIA_KEY
                fillCameraMonitorViewParent = true
                cameraName = linearOpMode.hardwareMap.get(WebcamName::class.java, "webcam")
            }
            install(CargoDetector) {
                minimumConfidence = RobotConstants.CARGO_DETECTION_MIN_CONFIDENCE
                useObjectTracker = true
            }
            install(MoveActionDefaults) {
                defaultPowerManager(MoveActionType.DRIVE,
                    ConstantPowerManager(0.75)
                )
                defaultPowerManager(MoveActionType.TURN,
                    ConstantPowerManager(0.75)
                )
            }
        }
    }

