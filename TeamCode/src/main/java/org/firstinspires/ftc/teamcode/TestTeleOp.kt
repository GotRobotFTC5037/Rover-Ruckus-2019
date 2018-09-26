package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotorSimple

@TeleOp
class TestTeleOp: LinearOpMode() {
    override fun runOpMode() {
        val leftMotor = hardwareMap.dcMotor.get("left motor").apply { direction = DcMotorSimple.Direction.REVERSE }
        val rightMotor = hardwareMap.dcMotor.get("right motor")

        waitForStart()
        while (opModeIsActive()) {

            var leftMotorPower = -gamepad1.left_stick_y.toDouble()
            var rightMotorPower = -gamepad1.right_stick_y.toDouble()
            leftMotor.power = leftMotorPower
            rightMotor.power = rightMotorPower

        }

        leftMotor.power = 0.0
        rightMotor.power = 0.0
    }

}
