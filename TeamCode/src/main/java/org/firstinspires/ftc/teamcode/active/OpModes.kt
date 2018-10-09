package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.gotrobot.grbase.MoveAction
import us.gotrobot.grbase.RobotTankDriveTrain
import us.gotrobot.grbase.createRobot

@Autonomous
class LibAutonomous : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        val robot = createRobot(this) {
            install(RobotTankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
        }

        robot.runAction(MoveAction.timeDrive(1000, 0.5))
        robot.runAction(MoveAction.turnTo(-135.0, 0.5))
        robot.runAction(MoveAction.timeDrive(1000, 0.5))
    }
}