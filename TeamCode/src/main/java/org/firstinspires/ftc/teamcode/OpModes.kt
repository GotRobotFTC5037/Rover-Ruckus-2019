package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.experimental.runBlocking
import org.firstinspires.ftc.teamcode.lib.RobotAction

@Autonomous
class RobotLibraryTest: LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        val robot = TestRobot(this)
        waitForStart()
        runBlocking { robot.runAction(testAction).join() }
    }

    companion object {
        val testAction = RobotAction.customAction {

        }
    }

}
