@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active.test

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.active.features.RobotLift
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.robot.robot

@TeleOp
class RobotLiftTest : LinearOpMode() {
    override fun runOpMode() {
        robot(this) {
            install(RobotLift) {
                liftMotorName = "lift"
            }
        }.perform {
            val lift = requestFeature(RobotLift)
            val position = lift.liftPosition.openSubscription()
            while (true) {
                when {
                    gamepad1.a -> lift.setPower(1.0)
                    gamepad1.b -> lift.setPower(-1.0)
                    else -> lift.setPower(0.0)
                }
                telemetry.addData("Lift Position", position.receive())
                telemetry.update()
                yield()
            }
        }
    }
}