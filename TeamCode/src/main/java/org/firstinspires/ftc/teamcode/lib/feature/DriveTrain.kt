package org.firstinspires.ftc.teamcode.lib.feature

import com.qualcomm.robotcore.hardware.DcMotorSimple

typealias MotorDirection = DcMotorSimple.Direction

interface DriveTrain : Feature {
    fun stop()
}

class InvalidDriveTrainOperationException(override val message: String? = null) : RuntimeException()