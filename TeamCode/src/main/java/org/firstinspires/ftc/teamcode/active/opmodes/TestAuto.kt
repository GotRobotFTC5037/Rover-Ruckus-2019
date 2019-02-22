@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import org.firstinspires.ftc.teamcode.active.RobotConstants
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.HeadingCorrection
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.util.Timeout
import org.firstinspires.ftc.teamcode.lib.util.with

@Disabled
@Autonomous(name = "Wall Follow Test", group = "Tests")
class WallFollowingTest : LinearOpMode() {
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@WallFollowingTest, this, shouldUseCamera = false).perform(
                wallFollowingDrive(
                    WallFollowingData(
                        99999.0,
                        7.5,
                        0.15,
                        RobotConstants.RightRangeSensor
                    )
                )
            )
    }

}

@Disabled
@Autonomous(name = "Timeout Test", group = "Tests")
class TimeoutTest : LinearOpMode() {
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@TimeoutTest, this, shouldUseCamera = false)
            .perform(drive(10_000.0) with Timeout(5000))

    }
}

@Autonomous(name = "Sensor Test", group = "Tests")
class SensorTest : LinearOpMode() {
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@SensorTest, this, shouldUseCamera = false)
            .perform {
                val rangeSensor = requestFeature(RobotConstants.RightRangeSensor)
                while (isActive) {
                    telemetry.addLine("Right Distance: ${rangeSensor.distance}")
                    telemetry.update()
                }
            }
    }
}

