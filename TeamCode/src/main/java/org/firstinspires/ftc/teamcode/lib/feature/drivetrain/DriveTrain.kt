package org.firstinspires.ftc.teamcode.lib.feature.drivetrain

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import org.firstinspires.ftc.teamcode.lib.pipeline.Pipeline

interface DriveTrain

interface LinearDriveTrain : DriveTrain {
    suspend fun setLinearPower(power: Double)
}

interface LateralDriveTrain : DriveTrain {
    suspend fun setLateralPower(power: Double)
}

interface RotationalDriveTrain : DriveTrain {
    suspend fun setRotationalPower(power: Double)
}

interface OmnidirectionalDriveTrain : LinearDriveTrain, LateralDriveTrain, RotationalDriveTrain {
    suspend fun setDirectionPower(
        linearPower: Double,
        lateralPower: Double,
        rotationalPower: Double
    )
}

interface DriveTrainMotorPowers {
    fun adjustHeadingPower(power: Double)
}

interface InterceptableDriveTrain<T : DriveTrainMotorPowers> {
    val powerPipeline: Pipeline<T, DriveTrain>
}

fun DcMotor.toDcMotorEx() = this as DcMotorEx