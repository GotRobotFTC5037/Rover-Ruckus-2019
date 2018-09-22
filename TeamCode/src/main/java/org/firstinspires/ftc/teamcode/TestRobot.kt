package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.lib.Robot
import org.firstinspires.ftc.teamcode.lib.RobotDriveTrain
import org.firstinspires.ftc.teamcode.lib.RobotTankDriveTrain

class TestRobot(override val linearOpMode: LinearOpMode) : Robot() {
    override val driveTrain: RobotDriveTrain = RobotTankDriveTrain()
}