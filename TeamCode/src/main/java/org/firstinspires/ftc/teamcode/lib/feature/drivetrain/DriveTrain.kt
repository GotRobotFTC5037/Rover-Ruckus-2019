package org.firstinspires.ftc.teamcode.lib.feature.drivetrain

import org.firstinspires.ftc.teamcode.lib.feature.Feature

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