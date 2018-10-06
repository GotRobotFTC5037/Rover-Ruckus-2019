package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode

typealias DriveTrain = RobotDriveTrain

abstract class RobotDriveTrain: Component {
    abstract override fun setup(linearOpMode: LinearOpMode)
    abstract override fun start()
    abstract fun setPower(vector: RobotVector)
    override fun stop() = setPower(RobotVector(0.0, 0.0, 0.0))
}
