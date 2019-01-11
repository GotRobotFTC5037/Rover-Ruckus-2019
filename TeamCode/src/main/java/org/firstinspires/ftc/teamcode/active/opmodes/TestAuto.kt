package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.runBlocking
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.action.actionSequenceOf
import org.firstinspires.ftc.teamcode.lib.action.drive
import org.firstinspires.ftc.teamcode.lib.action.turnTo

@Autonomous
class Test : LinearOpMode() {
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@Test, this, shouldUseCamera = false).perform(
            actionSequenceOf(
                turnTo(0.0),
                drive(50.0),
                turnTo(180.0),
                drive(50.0),
                turnTo(0.0)
            )
        )
    }

}

