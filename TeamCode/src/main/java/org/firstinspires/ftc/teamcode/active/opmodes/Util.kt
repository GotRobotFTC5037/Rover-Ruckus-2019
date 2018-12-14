@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.runBlocking
import org.firstinspires.ftc.teamcode.active.RobotLift
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.robot.robot

@Autonomous
class RetractLift : LinearOpMode() {
    override fun runOpMode() = runBlocking {
        robot(this@RetractLift, this) {
            install(RobotLift) {
                liftMotorName = "lift motor"
            }
        }.perform {
            val lift = requestFeature(RobotLift)
            lift.retract()
        }
    }
}