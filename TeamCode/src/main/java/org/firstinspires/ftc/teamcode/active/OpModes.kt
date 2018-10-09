package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.gotrobot.grbase.IMULocalizer
import us.gotrobot.grbase.RobotMoveAction
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
            install(RobotTankDriveTrain.PositionLocalizer)
            install(IMULocalizer)
        }

        robot.runAction(RobotMoveAction.timeDrive(1000, 0.5))
        robot.runAction(RobotMoveAction.turnTo(-135.0, 0.5))
        robot.runAction(RobotMoveAction.timeDrive(1000, 0.5))
    }
}