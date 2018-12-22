package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.channels.any
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.DriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrainLocalizer
import org.firstinspires.ftc.teamcode.lib.power
import kotlin.math.abs

fun drive(deltaDistance: Double): MoveAction = move {
    when (val driveTrain = requestFeature(DriveTrain::class)) {
        is TankDriveTrain -> {
            val localizer = requestFeature(TankDriveTrainLocalizer)
            val positionChannel = localizer.newPositionChannel()
            val driveTrainJob = launch {
                while (true) {
                    driveTrain.setMotorPowers(power(), power())
                    yield()
                }
            }.apply {
                invokeOnCompletion {
                    driveTrain.stop()
                }
            }
            positionChannel.any { abs(it.average) > abs(deltaDistance) }
            driveTrainJob.cancel()
        }
    }
}
