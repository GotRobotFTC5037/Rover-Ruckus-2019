package org.firstinspires.ftc.teamcode

import android.support.annotation.IntegerRes
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple

@Autonomous
class TestAuto : LinearOpMode() {

    val leftMotor: DcMotor by lazy {
        hardwareMap.dcMotor.get("left motor").apply {
            direction = DcMotorSimple.Direction.REVERSE
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }
    }

    val rightMotor: DcMotor by lazy {
        hardwareMap.dcMotor.get("right motor").apply {
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

  /*  fun gyroTurn(heading: Double) {
        var gyroSensor =
       Assume start heading is 315.
       We know target heading from function call.
       Determine we need to turn right or left.
       Determine speed of motors.
       Turn the robot.
       Stop the turn at the perfect degree.

         225 degrees for depot.
                           X = degrees we want to go to.
                           4 numbers. 45, 135, 225, 315. *\

    }
}
