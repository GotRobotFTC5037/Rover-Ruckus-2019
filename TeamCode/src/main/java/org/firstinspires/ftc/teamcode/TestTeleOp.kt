package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotorSimple

@TeleOp
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
