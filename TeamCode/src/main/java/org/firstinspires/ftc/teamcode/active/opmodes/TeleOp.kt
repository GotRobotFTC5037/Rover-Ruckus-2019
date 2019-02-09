@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.*
import org.firstinspires.ftc.teamcode.active.features.CargoDeliverySystem
import org.firstinspires.ftc.teamcode.active.features.Lift
import org.firstinspires.ftc.teamcode.active.features.MarkerDeployer
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.util.clip
import org.firstinspires.ftc.teamcode.lib.util.delayUntilStop
import org.firstinspires.ftc.teamcode.lib.util.loop


@TeleOp
class TeleOp : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@TeleOp, this).perform {
            val driveTrain = requestFeature(TankDriveTrain)
            val lift = requestFeature(Lift)
            while (isActive) {
                driveTrain.setMotorPowers(
                    TankDriveTrain.MotorPowers(
                        left = -gamepad1.left_stick_y.toDouble(),
                        right = -gamepad1.right_stick_x.toDouble()
                    )
                )
                when {
                    gamepad1.dpad_up -> lift.setPower(1.0)
                    gamepad1.dpad_down -> lift.setPower(-1.0)
                    else -> lift.setPower(0.0)
                }
            }
        }
    }
}


