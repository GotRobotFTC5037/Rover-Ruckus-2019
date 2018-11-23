package org.firstinspires.ftc.teamcode.lib.feature

import com.qualcomm.robotcore.hardware.DcMotorSimple

typealias MotorDirection = DcMotorSimple.Direction

interface DriveTrain : Feature {
    fun setPower(linearPower: Double, lateralPower: Double)
    fun setHeadingPower(power: Double)
    fun stopAllMotors()
}

class InvalidDriveTrainOperationException(override val message: String? = null) : RuntimeException()