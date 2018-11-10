@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.isActive
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.robot.perform

@TeleOp
class TeleOp : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        roverRuckusRobot(this).perform {
            val driveTrain = requestFeature(TankDriveTrain)
            val lift = requestFeature(RobotLift)
            while (isActive) {
                telemetry.addData("Button", lift.liftButton.isPressed)
                telemetry.update()

                driveTrain.setMotorPowers(
                    -gamepad1.left_stick_y.toDouble(),
                    -gamepad1.right_stick_y.toDouble()
                )
                when {
                    gamepad1.a -> lift.setPower(1.0)
                    gamepad1.b -> lift.setPower(-1.0)
                    else -> lift.setPower(0.0)
                }
            }
        }
    }

}
