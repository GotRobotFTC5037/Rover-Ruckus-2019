package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.teamcode.lib.Robot
import org.firstinspires.ftc.teamcode.lib.RobotMoveAction
import org.firstinspires.ftc.teamcode.lib.RobotTankDriveTrain

class TestRobot(linearOpMode: LinearOpMode) : Robot(linearOpMode) {

    override val driveTrain by lazy {
        RobotTankDriveTrain().apply {
            motors.add(
                RobotTankDriveTrain.Motor(
                    linearOpMode.hardwareMap.dcMotor.get("left motor").apply {
                        direction = DcMotorSimple.Direction.REVERSE
                    },
                    RobotTankDriveTrain.Side.LEFT
                )
            )
            motors.add(
                RobotTankDriveTrain.Motor(
                    linearOpMode.hardwareMap.dcMotor.get("right motor").apply {
                        direction = DcMotorSimple.Direction.FORWARD
                    },
                    RobotTankDriveTrain.Side.RIGHT
                )
            )
        }
    }

}

@Autonomous
class LibAutonomous : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        val robot = TestRobot(this)
        robot.setup()
        waitForStart()
        robot.start()
        robot.run(RobotMoveAction.linearTimeDrive(0.5, 500L))
        robot.run(RobotMoveAction.timeTurn(0.5, 1000L))
        robot.run(RobotMoveAction.linearTimeDrive(0.5, 500L))
        robot.stop()
    }

}

@TeleOp
class LibTeleOp : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {

    }

}