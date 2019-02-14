package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.runBlocking
import org.firstinspires.ftc.teamcode.active.RobotConstants
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.action.*

@Autonomous
class WallFollowingTest : LinearOpMode() {
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@WallFollowingTest, this, shouldUseCamera = false)
            .perform(
                wallFollowingDrive(
                    WallFollowingData(
                        99999.0,
                        7.5,
                        0.25,
                        RobotConstants.RightRangeSensor)
                )
            )
    }

}

