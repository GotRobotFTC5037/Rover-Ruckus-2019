package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.teamcode.lib.Robot
import org.firstinspires.ftc.teamcode.lib.RobotDriveTrain
import org.firstinspires.ftc.teamcode.lib.RobotTankDriveTrain

class TestRobot(override val opMode: LinearOpMode) : Robot() {
    override val driveTrain: RobotDriveTrain = RobotTankDriveTrain().apply {
        leftMotors.add(opMode.hardwareMap.dcMotor.get("left motor").apply {
            direction = DcMotorSimple.Direction.REVERSE
        })
        rightMotors.add(opMode.hardwareMap.dcMotor.get("right motor").apply {
            direction = DcMotorSimple.Direction.FORWARD
        })
    }
}