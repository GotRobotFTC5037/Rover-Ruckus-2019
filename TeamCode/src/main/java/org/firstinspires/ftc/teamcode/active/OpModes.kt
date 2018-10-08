package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.gotrobot.grbase.MoveAction
import us.gotrobot.grbase.TankDrive
import us.gotrobot.grbase.createRobot

@Autonomous
class LibAutonomous : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        val robot = createRobot(this) {
            addComponent(TankDrive) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
        }

        robot.runAction(MoveAction.timeDrive(1000, 0.5))
        robot.runAction(MoveAction.timeTurn(1000, 0.5))
        robot.runAction(MoveAction.timeDrive(1000, 0.5))
        robot.joinCurrentAction()
    }
}
