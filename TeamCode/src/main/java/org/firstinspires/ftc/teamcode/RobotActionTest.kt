package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode

@Autonomous
class RobotActionTest : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        with(TestRobot(this)) {
            setupAndWaitForStart()

            waitForActionsToComplete()
        }
    }

}