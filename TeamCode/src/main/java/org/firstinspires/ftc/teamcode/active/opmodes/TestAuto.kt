package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.runBlocking
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.action.actionSequenceOf
import org.firstinspires.ftc.teamcode.lib.action.drive
import org.firstinspires.ftc.teamcode.lib.action.repeat
import org.firstinspires.ftc.teamcode.lib.action.turn

class Test : LinearOpMode() {
    override fun runOpMode() = runBlocking {
        val robot = roverRuckusRobot(this@Test, this)
        robot.perform(
            repeat(actionSequenceOf(drive(50.0), turn(90.0)), 4)
        )
    }

}

