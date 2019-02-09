package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.action.wait
import org.firstinspires.ftc.teamcode.lib.util.cancelAndJoin
import org.firstinspires.ftc.teamcode.lib.util.delayUntilStart

@TeleOp
class PrimitiveTeleOp: LinearOpMode() {

    override fun runOpMode() {
        val leftMotor = hardwareMap.get(DcMotor::class.java, "left motor").apply {
            direction = DcMotorSimple.Direction.REVERSE
        }
        val rightMotor = hardwareMap.get(DcMotor::class.java, "right motor").apply {
            direction = DcMotorSimple.Direction.FORWARD
        }

        waitForStart()
        
        while (opModeIsActive()) {
            leftMotor.power = -gamepad1.left_stick_y.toDouble()
            rightMotor.power = -gamepad1.right_stick_y.toDouble()
        }

    }

}
