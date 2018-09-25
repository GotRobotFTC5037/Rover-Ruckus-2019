package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.hardware.DcMotor

abstract class RobotDriveTrain {
    abstract fun setup()
    abstract fun setLinearDrivePower(power: Double)
    abstract fun stop()
}

class RobotTankDriveTrain : RobotDriveTrain() {

    val leftMotors = mutableListOf<DcMotor>()
    val rightMotors = mutableListOf<DcMotor>()

    override fun setup() {
        for (motor in leftMotors + rightMotors) {
            motor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            motor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }
    }

    fun setMotorPowers(leftPower: Double, rightPower: Double) {
        for (leftMotor in leftMotors) {
            leftMotor.power = leftPower
        }
        for (rightMotor in rightMotors) {
            rightMotor.power = rightPower
        }
    }

    override fun setLinearDrivePower(power: Double) {
        setMotorPowers(power, power)
    }

    override fun stop() {
        for (motor in leftMotors + rightMotors) {
            motor.power = 0.0
        }
    }

}



