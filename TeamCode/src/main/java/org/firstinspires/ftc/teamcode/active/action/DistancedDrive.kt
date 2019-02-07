package org.firstinspires.ftc.teamcode.active.action

import kotlinx.coroutines.channels.any
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.action.Drive
import org.firstinspires.ftc.teamcode.lib.action.MoveAction
import org.firstinspires.ftc.teamcode.lib.action.MoveActionContext
import org.firstinspires.ftc.teamcode.lib.action.move
import org.firstinspires.ftc.teamcode.lib.feature.FeatureKey
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.DriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrainLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.sensor.RangeSensor
import org.firstinspires.ftc.teamcode.lib.power
import kotlin.math.abs

fun distancedDrive(
    deltaDriveDistance: Double,
    targetDistance: Double,
    rangeSensorKey: FeatureKey<RangeSensor>,
    coefficient: Double
): MoveAction = move {
    when (val driveTrain = requestFeature(DriveTrain::class)) {
        is TankDriveTrain -> {
            val localizer = requestFeature(TankDriveTrainLocalizer)
            val rangeSensor = requestFeature(rangeSensorKey)

            fun adjustmentPower() = (rangeSensor.distance - targetDistance) * coefficient

            val positionChannel = localizer.newPositionChannel()
            val driveTrainJob = launch {
                if (deltaDriveDistance > 0.0) {
                    while (true) {
                        driveTrain.setMotorPowers(leftPower = power() + adjustmentPower(), rightPower = power() - adjustmentPower())
                        yield()
                    }
                } else if (deltaDriveDistance < 0.0) {
                    while (true) {
                        driveTrain.setMotorPowers(leftPower = -power() - adjustmentPower(), rightPower = -power() - adjustmentPower())
                        yield()
                    }
                }
            }.apply {
                invokeOnCompletion {
                    driveTrain.stop()
                }
            }
            positionChannel.any {
                val target = abs(deltaDriveDistance)
                val currentPosition = abs(it.average)

                telemetry.addLine()
                    .addData("Target", deltaDriveDistance)
                    .addData("Current", currentPosition)
                telemetry.update()

                currentPosition > target
            }
            driveTrainJob.cancel()
        }
    }
}.apply {
    context = MoveActionContext(Drive(deltaDriveDistance))
}