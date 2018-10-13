package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.hardware.DcMotorSimple

typealias MotorDirection = DcMotorSimple.Direction

interface RobotDriveTrain {

    fun setPower(linearPower: Double, lateralPower: Double)
    fun setHeadingPower(power: Double)
    fun stopAllMotors()
}