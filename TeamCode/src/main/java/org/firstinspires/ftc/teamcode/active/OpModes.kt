package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.lib.RobotMoveAction
import org.firstinspires.ftc.teamcode.lib.RobotTankDriveTrain
import org.firstinspires.ftc.teamcode.lib.createRobot

@Autonomous
@Disabled
class LibAutonomous : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        val robot = createRobot(this) {
            install(RobotTankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
        }
        robot.runAction(RobotMoveAction.timeDrive(3000,0.5))
        robot.runAction(RobotMoveAction.timeTurn(1500,-0.5))
        robot.runAction(RobotMoveAction.timeDrive(1000,-0.5))
    }

}
