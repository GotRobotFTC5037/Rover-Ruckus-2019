package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.gotrobot.grbase.action.ConstantPowerManager
import us.gotrobot.grbase.feature.DefaultPowerManager
import us.gotrobot.grbase.feature.HeadingCorrection
import us.gotrobot.grbase.feature.TargetHeading
import us.gotrobot.grbase.feature.drivercontrol.DriverControl
import us.gotrobot.grbase.feature.drivetrain.MecanumDriveTrain
import us.gotrobot.grbase.feature.localizer.IMULocalizer
import us.gotrobot.grbase.opmode.isAutonomous
import us.gotrobot.grbase.opmode.isTeleOp
import us.gotrobot.grbase.robot.install
import us.gotrobot.grbase.robot.robot

object Metabot {
    const val FRONT_LEFT_MOTOR = "front left motor"
    const val FRONT_RIGHT_MOTOR = "front right motor"
    const val BACK_LEFT_MOTOR = "back left motor"
    const val BACK_RIGHT_MOTOR = "back right motor"

    const val EXTENSION_MOTOR = "extension motor"
    const val ROTATION_MOTOR = "rotation motor"
    const val INTAKE_MOTOR = "intake motor"
    const val SORTING_SERVO = "sorting servo"

    const val MARKER_DEPLOYER_SERVO = "marker servo"

    const val LIFT_MOTOR = "lift motor"
    const val LIMIT_SWITCH = "limit switch"

    const val IMU = "imu"

    const val HEADING_CORRECTION_COEFFICIENT = 0.015

    const val POWER_MANAGER_VALUE = 0.60

    const val WHEEL_DIAMETER = 15.50
    const val GEAR_RATIO = 1.0

    @Suppress("SpellCheckingInspection")
    const val VUFORIA_LICENCE_KEY =
        "AdIaYr//////AAABmbPW4cADC0JWmq5z8YPKV2BLhjRavE34U++fSDpW2nfDwTsg99Uz5YWBQL02Wgz62sORWmPO" +
                "VnNooNp87t4XrMIK8NlZKHJ8oFBfHsmar4sODJt7hqSUy3ZeUMfGsCUQyh8J/dHrFdGQJ7EdTJUdB8XM" +
                "K+urV2h51WpIyaZCL1Aa1BjNBODanTcX2yFTMDjno9QIbzQZ3ZfFwy6Nx/y196DvIa8/47/y0x2OLFzc" +
                "VpeiUvDwtKKc9CzrAUVSpd8/qLcOKPTKy5VUxRawILhbovkLTntIzBFtikuLp9kqqrysX4kW2gzW2H4X" +
                "jF2z+cqrypKT8dwHCsLlEdcS1jXBVlfbExfj+7efvMPP3dSi4Zjo"
}

@Suppress("FunctionName")
suspend fun OpMode.Metabot() = robot {

    // Drive Train
    install(MecanumDriveTrain) {
        frontLeftMotorName = Metabot.FRONT_LEFT_MOTOR
        frontRightMotorName = Metabot.FRONT_RIGHT_MOTOR
        backLeftMotorName = Metabot.BACK_LEFT_MOTOR
        backRightMotorName = Metabot.BACK_RIGHT_MOTOR
    }

    // Hardware
//    val extensionMotor = install(ManagedMotor, )

    // Sub-components
    install(CargoDeliverySystem) {
        extensionMotorName = Metabot.EXTENSION_MOTOR
        rotationMotorName = Metabot.ROTATION_MOTOR
        intakeMotorName = Metabot.INTAKE_MOTOR
        sortingServoName = Metabot.SORTING_SERVO
    }
    install(MarkerDeployer) {
        servoName = Metabot.MARKER_DEPLOYER_SERVO
    }
    install(RobotLift) {
        liftMotorName = Metabot.LIFT_MOTOR
        limitSwitchName = Metabot.LIMIT_SWITCH
    }

    // Sensors
    install(IMULocalizer) {
        imuName = Metabot.IMU
    }

    // Discrete Features
    install(TargetHeading) {
        headingLocalizerKey = IMULocalizer
    }
    install(HeadingCorrection) {
        coefficient = Metabot.HEADING_CORRECTION_COEFFICIENT
    }

    // Game Mode Specific
    if (isAutonomous) {
        install(MecanumDriveTrain.Localizer) {
            wheelDiameter = Metabot.WHEEL_DIAMETER
            gearRatio = Metabot.GEAR_RATIO
        }
        install(DefaultPowerManager) {
            powerManager = ConstantPowerManager(Metabot.POWER_MANAGER_VALUE)
        }
    } else if (isTeleOp) {
        install(DriverControl)
    }

}
