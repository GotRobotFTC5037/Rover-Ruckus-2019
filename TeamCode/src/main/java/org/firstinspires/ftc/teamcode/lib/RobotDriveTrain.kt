package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.hardware.DcMotor

sealed class RobotDriveTrain {
    abstract fun setup()
    abstract fun start()
    abstract fun setPower(vector: RobotVector)
    fun stop() = setPower(RobotVector(0.0, 0.0, 0.0))
}

object EmptyRobotDriveTrain : RobotDriveTrain() {

    override fun setup() {
        // Does nothing.
    }

    override fun start() {
        // Does nothing.
    }

    override fun setPower(vector: RobotVector) {
        // Does nothing.
    }

}

class RobotTankDriveTrain : RobotDriveTrain() {

    val motors = mutableListOf<Motor>()

    data class Motor (
        val dcMotor: DcMotor,
        val side: Side
    )

    enum class Side {
        LEFT, RIGHT
    }

    override fun setup() {
        for (motor in motors) {
            motor.dcMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            motor.dcMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        }
    }

    override fun start() {
        for (motor in motors) {
            motor.dcMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }
    }

    fun setMotorPowers(leftPower: Double, rightPower: Double) {
        for (motor in motors.filter { it.side == Side.LEFT }) {
            motor.dcMotor.power = leftPower
        }
        for (motor in motors.filter { it.side == Side.RIGHT }) {
            motor.dcMotor.power = rightPower
        }
    }

    override fun setPower(vector: RobotVector) {
        when {
            vector.lateral != 0.0 ->
                throw UnsupportedOperationException(
                    "Lateral Power must be set to 0.0 for RobotTankDriveTrain."
                )

            ((vector.linear == 0.0) or (vector.heading == 0.0)).not() ->
                throw UnsupportedOperationException(
                    "Linear Power and Heading Power can only be set exclusively for RobotTankDriveTrain."
                )

            else -> {
                when {
                    vector.linear != 0.0 -> setMotorPowers(vector.linear, vector.linear)
                    vector.heading != 0.0 -> setMotorPowers(-vector.heading, vector.heading)
                    else -> setMotorPowers(0.0, 0.0)
                }
            }
        }
    }



}


