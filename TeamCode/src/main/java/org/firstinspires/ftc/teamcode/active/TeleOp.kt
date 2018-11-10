@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.robot.perform

@TeleOp
class TeleOp : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        roverRuckusRobot(this).perform {
            val driveTrain = requestFeature(TankDriveTrain)
            val lift = requestFeature(RobotLift)

            var reversed = false

            while (true) {
                if (!reversed) {
                    driveTrain.setMotorPowers(
                        -gamepad1.left_stick_y.toDouble(),
                        -gamepad1.right_stick_y.toDouble()
                    )
                } else {
                    driveTrain.setMotorPowers(
                        gamepad1.right_stick_y.toDouble(),
                        gamepad1.left_stick_y.toDouble()
                    )
                }

                when {
                    gamepad1.right_trigger > 0.5 -> lift.setPower(1.0)
                    gamepad1.left_trigger > 0.5 -> lift.setPower(-1.0)
                    else -> lift.setPower(0.0)
                }

                if (gamepad1.start) {
                    reversed = !reversed
                    while (gamepad1.start) {
                        yield()
                    }
                }

                yield()
            }
        }
    }

}
