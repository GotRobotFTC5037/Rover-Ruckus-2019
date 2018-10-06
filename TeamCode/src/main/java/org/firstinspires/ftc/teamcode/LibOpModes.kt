package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.lib.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.MoveAction
import org.firstinspires.ftc.teamcode.lib.TankDrive
import org.firstinspires.ftc.teamcode.lib.createRobot

@Autonomous
class LibAutonomous : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        val robot = createRobot(this) {
            driveTrain(TankDrive::class) {
                leftMotor("left motor")
                rightMotor("right motor")
            }
            setLocalizer(IMULocalizer::class)

        }
        robot.setupAndWaitForStart()
        robot.runAction(MoveAction.linearTimeDrive(1500L, 0.5))
        robot.runAction(MoveAction.linearTimeDrive(500L, -0.5))
        robot.runAction(MoveAction.turnTo(135.0, 0.5))
        robot.runAction(MoveAction.linearTimeDrive(3500L, 0.5))
    }

}
