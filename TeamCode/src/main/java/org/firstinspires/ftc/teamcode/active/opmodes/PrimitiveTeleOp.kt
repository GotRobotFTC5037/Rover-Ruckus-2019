package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.util.cancelAndJoin
import org.firstinspires.ftc.teamcode.lib.util.delayUntilStart

@TeleOp
class PrimitiveTeleOp: LinearOpMode() {

    override fun runOpMode() = runBlocking {
        val frontLeftMotor = hardwareMap.get(DcMotor::class.java, "front left motor")
        val frontRightMotor = hardwareMap.get(DcMotor::class.java, "front right motor")
        val backLeftMotor = hardwareMap.get(DcMotor::class.java, "back left motor")
        val backRightMotor = hardwareMap.get(DcMotor::class.java, "back right motor")

        while (!isStarted) {
            yield()
        }
        
        launch {
            while (true) {
                frontLeftMotor.power = -gamepad1.left_stick_y.toDouble()
                backLeftMotor.power = -gamepad1.left_stick_y.toDouble()
                yield()
            }
        }

        launch {
            while (true) {
                frontRightMotor.power = -gamepad1.right_stick_y.toDouble()
                backRightMotor.power = -gamepad1.right_stick_y.toDouble()
                yield()
            }
        }

        cancelAndJoin()
        return@runBlocking
    }

}