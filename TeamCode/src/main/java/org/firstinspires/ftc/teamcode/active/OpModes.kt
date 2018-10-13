package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.lib.*

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
        robot.setupAndWaitForStart()
        robot.runAction(RobotMoveAction.timeTurn(400,0.3))
        robot.runAction(RobotMoveAction.timeDrive(875,0.45))
        robot.runAction(RobotMoveAction.timeDrive(100,0.0))
        robot.runAction(RobotMoveAction.timeTurn(300,-0.4))
        robot.runAction(RobotMoveAction.timeDrive(650,0.5))
        robot.runAction(RobotMoveAction.timeDrive(550,-0.7))
        robot.runAction(RobotMoveAction.timeTurn(550, -0.5))
        robot.runAction(RobotMoveAction.timeDrive(100,0.0))
        robot.runAction(RobotMoveAction.timeDrive(3500, 0.7))




        //robot.runAction(RobotMoveAction.timeDrive(1500L,0.5))
        //robot.runAction(RobotMoveAction.timeDrive(500L,-0.5))
        //robot.runAction(RobotMoveAction.timeTurn(900,-0.4))
        //robot.runAction(RobotMoveAction.timeDrive(3750,1.0))
    }

}

