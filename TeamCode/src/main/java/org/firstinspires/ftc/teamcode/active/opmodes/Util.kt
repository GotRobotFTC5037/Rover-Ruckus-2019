@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.runBlocking
import org.firstinspires.ftc.teamcode.active.RobotLift
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.robot.perform

@Autonomous
class RetractLift : LinearOpMode() {
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@RetractLift).perform {
            val lift = requestFeature(RobotLift)
            lift.retract()
        }
    }
}