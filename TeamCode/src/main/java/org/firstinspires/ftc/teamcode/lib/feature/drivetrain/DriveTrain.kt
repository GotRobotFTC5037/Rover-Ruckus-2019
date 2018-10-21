package org.firstinspires.ftc.teamcode.lib.feature.drivetrain

import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.teamcode.lib.feature.Feature

typealias MotorDirection = DcMotorSimple.Direction

interface DriveTrain : Feature {
    fun setPower(linearPower: Double, lateralPower: Double)
    fun setHeadingPower(power: Double)
    fun stopAllMotors()
}

class InvalidDriveTrainOperationException(override val message: String? = null) : RuntimeException()