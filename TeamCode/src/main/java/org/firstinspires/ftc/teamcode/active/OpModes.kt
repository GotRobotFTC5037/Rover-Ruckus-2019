package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.AnalogInput
import org.firstinspires.ftc.teamcode.lib.Robot
import org.firstinspires.ftc.teamcode.lib.RobotMoveAction
import org.firstinspires.ftc.teamcode.lib.RobotTankDriveTrain
import org.firstinspires.ftc.teamcode.lib.createRobot

@Autonomous
class LibAutonomous : LinearOpMode() {

    private val potentiometer: AnalogInput by lazy {
        hardwareMap.analogInput.get("potentiometer")
    }

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        val robot = createRobot(this) {
            install(RobotTankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")

            }
        }

        robot.setupAndWaitForStart()
        when {
            potentiometer.voltage < 1.1 -> {
                left(robot)
            }

            potentiometer.voltage > 2.2 -> {
                right(robot)
            }

            potentiometer.voltage > 1.1 && potentiometer.voltage < 2.2 -> {
                center(robot)
            }

        }
    }

    fun left(robot: Robot) {
        robot.runAction(RobotMoveAction.timeTurn(425, -0.3))
        sleep(100)
        robot.runAction(RobotMoveAction.timeDrive(650, 0.5))
        sleep(100)
        robot.runAction(RobotMoveAction.timeTurn(325, 0.45))
        sleep(100)
        robot.runAction(RobotMoveAction.timeDrive(550, 0.5))
        sleep(50)
        robot.runAction(RobotMoveAction.timeDrive(500, -0.2))
        sleep(100)
        robot.runAction(RobotMoveAction.timeTurn(225, 0.4))
        sleep(100)
        robot.runAction(RobotMoveAction.timeDrive(700, -0.5))
    }

    fun center(robot: Robot) {
        robot.runAction(RobotMoveAction.timeDrive(1500L, 0.5))
        robot.runAction(RobotMoveAction.timeDrive(500L, -0.5))
        robot.runAction(RobotMoveAction.timeTurn(900, -0.4))
        robot.runAction(RobotMoveAction.timeDrive(500, 1.0))
    }

    fun right(robot: Robot) {
        robot.runAction(RobotMoveAction.timeTurn(400, 0.3))
        robot.runAction(RobotMoveAction.timeDrive(875, 0.45))
        sleep(100)
        robot.runAction(RobotMoveAction.timeTurn(300, -0.4))
        robot.runAction(RobotMoveAction.timeDrive(650, 0.5))
        robot.runAction(RobotMoveAction.timeTurn(550, -0.5))
        sleep(100)
        robot.runAction(RobotMoveAction.timeDrive(50, 0.7))
        robot.runAction(RobotMoveAction.timeTurn(100, -0.35))
        robot.runAction(RobotMoveAction.timeDrive(500, 0.7))
    }

}

