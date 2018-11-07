@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.robot.robot

@TeleOp
class TeleOp : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        robot(this) {
            install(TankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
            install(RobotLift) {
                liftMotorName = "lift"
            }
        }.perform {
            val driveTrain = requestFeature(TankDriveTrain)
            val lift = requestFeature(RobotLift)
            while (true) {
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
