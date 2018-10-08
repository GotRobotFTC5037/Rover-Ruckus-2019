package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.experimental.cancelChildren
import kotlinx.coroutines.experimental.runBlocking

@TeleOp(name = "Archimedes TeleOp")
@Disabled
class RobotTeleOp : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() = runBlocking {
        val robot = Archimedes(this@RobotTeleOp, this)
        waitForStart()
        robot.startBallLauncher()
        while (opModeIsActive()) {
            robot.leftMotor.power = -gamepad1.left_stick_y.toDouble()
            robot.rightMotor.power = -gamepad1.right_stick_y.toDouble()
            robot.ballDeployer.position = if (gamepad1.right_trigger > 0.0) 0.25 else 0.00
            robot.ballCollector.power = when {
                gamepad1.left_bumper -> 1.0
                gamepad1.right_bumper -> -1.0
                else -> 0.0
            }
        }
        coroutineContext.cancelChildren()
    }

}