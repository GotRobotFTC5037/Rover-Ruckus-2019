package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.runBlocking
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.action.*

@Autonomous
class Test : LinearOpMode() {
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@Test, this, useCamera = false).perform(
            actionSequenceOf(
                drive(300.0),
                drive(-300.0)
                //turnTo(-90.0),
                //drive(50.0), turnTo(90.0),
                //drive(50.0), turnTo(0.0),
                //drive(-50.0)
            )
        )
    }

}

