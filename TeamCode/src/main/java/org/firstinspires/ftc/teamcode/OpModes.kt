package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple

@Autonomous
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

    override fun runOpMode() {
        waitForStart()

        drive(0.5, 2000)
        sleep(1000)
        drive(-0.5, -400)
        leftMotor.power = 0.3
        rightMotor.power = -0.3
        sleep(1250)
        rightMotor.power = 0.3
        drive(0.5, 5000)
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

        leftMotor.power = 0.0
        rightMotor.power = 0.0
    }
}
