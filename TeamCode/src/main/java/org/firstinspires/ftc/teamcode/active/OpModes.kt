package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.lib.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.RobotMoveAction
import org.firstinspires.ftc.teamcode.lib.RobotTankDriveTrain
import org.firstinspires.ftc.teamcode.lib.createRobot

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
        robot.runAction(RobotMoveAction.timeDrive(1500L,0.5))
        robot.runAction(RobotMoveAction.timeDrive(500L,-0.5))
        robot.runAction(RobotMoveAction.timeTurn(900,-0.4))
        robot.runAction(RobotMoveAction.timeDrive(3500,1.0))
    }

}
