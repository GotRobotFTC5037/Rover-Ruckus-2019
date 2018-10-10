package org.firstinspires.ftc.teamcode.active

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference
import kotlin.math.abs

@Autonomous
@Disabled
class TestAuto : LinearOpMode() {

    val leftMotor: DcMotor by lazy {
        hardwareMap.dcMotor.get("left addMotor").apply {
            direction = DcMotorSimple.Direction.REVERSE
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }
    }

    val rightMotor: DcMotor by lazy {
        hardwareMap.dcMotor.get("right addMotor").apply {
            direction = DcMotorSimple.Direction.FORWARD
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }
    }

    val imu: BNO055IMU by lazy {
        hardwareMap.get(BNO055IMU::class.java, "imu").apply {
            initialize(BNO055IMU.Parameters())
        }
    }

    val heading: Double
        get() = imu.getAngularOrientation(
            AxesReference.INTRINSIC,
            AxesOrder.ZYX,
            AngleUnit.DEGREES
        ).firstAngle.toDouble()

    fun BNO055IMU.waitForGyoCalibraion() {
        telemetry.addLine("calibrate gyro")
        telemetry.update()
        while (isGyroCalibrated.not()) {
            idle()
        }
        telemetry.update()
    }

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        waitForStart()
        imu.waitForGyoCalibraion()
        drive(0.6,1200)
        turn(0.5, 90.0)
        turn(0.5,-90.0)
    }

    fun drive(power: Double, distance: Int) {
        val initialRightMotorPosition = rightMotor.currentPosition
        val initialLeftMotorPosition = leftMotor.currentPosition
        rightMotor.power = power
        leftMotor.power = power
        when {
            distance > 0 -> while (
                leftMotor.currentPosition < initialLeftMotorPosition + distance &&
                rightMotor.currentPosition < initialRightMotorPosition + distance &&
                opModeIsActive()
            ) {
                idle()
            }
            distance < 0 -> while (
                leftMotor.currentPosition > initialLeftMotorPosition + distance &&
                rightMotor.currentPosition > initialRightMotorPosition + distance &&
                opModeIsActive()
            ) {
                idle()
            }
            else -> return
        }
    }



    fun turn(power: Double, targetHeading: Double) {
        val initialHeading = heading
        when {
            initialHeading > targetHeading -> {
                leftMotor.power = abs(power)
                rightMotor.power = -abs(power)
                while(heading > targetHeading){
                    telemetry.addLine("Heading: $heading")
                    telemetry.update()
                    idle()
                }

            }
            initialHeading < targetHeading -> {
                leftMotor.power = -abs(power)
                rightMotor.power = abs(power)
                while(heading < targetHeading) {
                    telemetry.addLine("Heading: $heading")
                    telemetry.update()
                    idle()
                }
            }
        }
        leftMotor.power = 0.0
        rightMotor.power = 0.0
    }
}

@TeleOp
@Disabled
class TestTeleOp: LinearOpMode() {
    override fun runOpMode() {
        val leftMotor = hardwareMap.dcMotor.get("left addMotor").apply { direction = DcMotorSimple.Direction.REVERSE }
        val rightMotor = hardwareMap.dcMotor.get("right addMotor")

        waitForStart()
        while (opModeIsActive()) {

            val leftMotorPower = -gamepad1.left_stick_y * -gamepad1.right_stick_x
            val rightMotorPower = -gamepad1.left_stick_y * gamepad1.right_stick_x
            leftMotor.power = leftMotorPower.toDouble()
            rightMotor.power = rightMotorPower.toDouble()

        }

        leftMotor.power = 0.0
        rightMotor.power = 0.0
    }

}