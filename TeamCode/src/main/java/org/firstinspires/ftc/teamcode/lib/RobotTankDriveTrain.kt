package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap

typealias TankDrive = RobotTankDriveTrain
typealias MotorDirection = DcMotorSimple.Direction

class RobotTankDriveTrain : RobotDriveTrain() {

    lateinit var linearOpMode: LinearOpMode
    val hardwareMap: HardwareMap get() = linearOpMode.hardwareMap

    private val motors = mutableListOf<DcMotor>()
    private val motorConfigurations = mutableMapOf<DcMotor, MotorConfiguration>()
    private val pendingMotors = mutableListOf<Triple<String, DcMotorSimple.Direction, MotorConfiguration>>()

    enum class MotorSide{
        LEFT, RIGHT
    }

    data class MotorConfiguration(
        val side: MotorSide
    )

    fun addMotor(motor: DcMotor, configuration: MotorConfiguration) {
        motors.add(motor)
        motorConfigurations[motor] = configuration
    }

    fun leftMotor(name: String, direction: MotorDirection = MotorDirection.REVERSE) {
        pendingMotors.add(Triple(name, direction, MotorConfiguration(MotorSide.LEFT)))
    }

    fun rightMotor(name: String, direction: MotorDirection = MotorDirection.FORWARD) {
        pendingMotors.add(Triple(name, direction, MotorConfiguration(MotorSide.RIGHT)))
    }

    override fun setup(linearOpMode: LinearOpMode) {
        this.linearOpMode = linearOpMode
        for (pendingMotor in pendingMotors) {
            val dcMotor = hardwareMap.get(DcMotor::class.java, pendingMotor.first).apply {
                direction = pendingMotor.second
            }
            addMotor(
                dcMotor,
                pendingMotor.third
            )
        }
        for (motor in motors) {
            motor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        }
    }

    override fun start() {
        for (motor in motors) {
            motor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }
    }

    fun setMotorPowers(leftPower: Double, rightPower: Double) {
        for (motor in motors) {
            motor.power = when (motorConfigurations[motor]?.side) {
                MotorSide.LEFT -> leftPower
                MotorSide.RIGHT -> rightPower
                null -> TODO("This should never happen.")
            }
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
