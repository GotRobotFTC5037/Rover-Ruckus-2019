package us.gotrobot.grbase

import com.qualcomm.robotcore.hardware.DcMotorSimple

typealias MotorDirection = DcMotorSimple.Direction

interface RobotDriveTrain {

    fun setPower(linearPower: Double, lateralPower: Double)
    fun setHeadingPower(power: Double)
    fun stopAllMotors()

    companion object Descriptor : RobotFeatureDescriptor<RobotDriveTrain> {
        override val key = RobotFeatureKey<RobotDriveTrain>("RobotDriveTrain")
    }
}