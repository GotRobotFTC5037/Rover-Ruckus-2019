package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.CoroutineScope
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.teamcode.active.RobotConstants.RightRangeSensor
import org.firstinspires.ftc.teamcode.active.features.*
import org.firstinspires.ftc.teamcode.lib.ConstantPowerManager
import org.firstinspires.ftc.teamcode.lib.NothingPowerManager
import org.firstinspires.ftc.teamcode.lib.action.Drive
import org.firstinspires.ftc.teamcode.lib.action.TurnTo
import org.firstinspires.ftc.teamcode.lib.action.UnspecifiedMoveActionType
import org.firstinspires.ftc.teamcode.lib.feature.*
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.MotorDirection
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrainLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.feature.sensor.RangeSensor
import org.firstinspires.ftc.teamcode.lib.feature.vision.Vuforia
import org.firstinspires.ftc.teamcode.lib.robot.install
import org.firstinspires.ftc.teamcode.lib.robot.robot
import org.firstinspires.ftc.teamcode.lib.robot.telemetry
import org.firstinspires.ftc.teamcode.lib.util.isAutonomous

@Suppress("SpellCheckingInspection")
object RobotConstants {


    const val LEFT_DRIVE_MOTOR = "left motor"
    const val RIGHT_DRIVE_MOTOR = "right motor"
    const val LIFT_MOTOR = "lift motor"
    const val MARKER_DEPLOYER_SERVO = "marker"
    const val INTAKE_LIFT_MOTOR = "intake lift"
    const val INTAKE_MOTOR = "intake"
    const val POPPER_MOTOR = "popper"
    const val IMU = "imu"
    const val WEBCAM = "webcam"
    const val WHEEL_DIAMETER = 10.16
    const val CARGO_DETECTION_MIN_CONFIDENCE = 0.45

    // Keys
    val RightRangeSensor = featureKey<RangeSensor>()

    // TODO: Put this key into a file and read from the file.
    const val VUFORIA_KEY = "AdIaYr//////AAABmbPW4cADC0JWmq5z8YPKV2BLhjRavE34U++fSDpW2nfDwTsg99Uz5YWBQL02Wgz62sORWmPOVnNooNp87t4XrMIK8NlZKHJ8oFBfHsmar4sODJt7hqSUy3ZeUMfGsCUQyh8J/dHrFdGQJ7EdTJUdB8XMK+urV2h51WpIyaZCL1Aa1BjNBODanTcX2yFTMDjno9QIbzQZ3ZfFwy6Nx/y196DvIa8/47/y0x2OLFzcVpeiUvDwtKKc9CzrAUVSpd8/qLcOKPTKy5VUxRawILhbovkLTntIzBFtikuLp9kqqrysX4kW2gzW2H4XjF2z+cqrypKT8dwHCsLlEdcS1jXBVlfbExfj+7efvMPP3dSi4Zjo"
}

@Suppress("SpellCheckingInspection")
suspend fun roverRuckusRobot(
    linearOpMode: LinearOpMode,
    coroutineScope: CoroutineScope,
    shouldUseCamera: Boolean = true
) = robot(linearOpMode, coroutineScope) {

    telemetry.msTransmissionInterval = 500

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
    install(CargoDeliverySystem) {
        intakeLift = RobotConstants.INTAKE_LIFT_MOTOR
        intake = RobotConstants.INTAKE_MOTOR
    }

    // Localizer
    install(IMULocalizer) {
        imuName = RobotConstants.IMU
        order = AxesOrder.ZYX
        initialHeading = 90.0
    }

    install(TiltTermination) {
        this.terminationAngle = 45.0
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
            Drive uses ConstantPowerManager(power = 1.0)
            TurnTo uses ConstantPowerManager(power = 1.0)
        }

    }
}
