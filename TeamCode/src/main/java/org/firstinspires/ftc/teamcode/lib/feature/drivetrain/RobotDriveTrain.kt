package org.firstinspires.ftc.teamcode.lib.feature.drivetrain

import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.teamcode.lib.feature.RobotComponent

typealias MotorDirection = DcMotorSimple.Direction

interface RobotDriveTrain : RobotComponent {
    fun setPower(linearPower: Double, lateralPower: Double)
    fun setHeadingPower(power: Double)
    fun stopAllMotors()
}

class InvalidDriveTrainOperationException(override val message: String? = null) : RuntimeException()