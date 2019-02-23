package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.channels.any
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.FeatureKey
import org.firstinspires.ftc.teamcode.lib.feature.HeadingCorrection
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrainLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.sensor.RangeSensor
import org.firstinspires.ftc.teamcode.lib.power
import kotlin.math.abs

data class WallFollowingData(
    var distance: Double,
    var wallDistance: Double,
    var coefficient: Double,
    var rangeSensorKey: FeatureKey<RangeSensor>
)

fun wallFollowingDrive(data: WallFollowingData) = move {
    val driveTrain = requestFeature(TankDriveTrain)
    val localizer = requestFeature(TankDriveTrainLocalizer)
    val rangeSensor = requestFeature(data.rangeSensorKey)

    val positionChannel = localizer.newPositionChannel()

    fun adjustmentPower(): Double {
        val distance = rangeSensor.distance
        return if (distance < 50) {
            (distance - data.wallDistance) * -data.coefficient
        } else {
            0.5
        }
    }

    val driveJob = launch {
        if (data.distance > 0.0) {
            while (true) {
                val adjustmentPower = adjustmentPower()
                driveTrain.setMotorPowers(
                    TankDriveTrain.MotorPowers(power() + adjustmentPower, power() - adjustmentPower)
                )
                yield()
            }
        } else if (data.distance < 0.0) {
            while (true) {
                val adjustmentPower = adjustmentPower()
                driveTrain.setMotorPowers(
                    TankDriveTrain.MotorPowers(-power() + adjustmentPower, -power() - adjustmentPower)
                )
                yield()
            }
        }
    }.apply {
        invokeOnCompletion {
            driveTrain.stop()
        }
    }

    positionChannel.any {
        val target = abs(data.distance)
        val currentPosition = abs(it.average)

        telemetry.addLine()
            .addData("Target", data.distance)
            .addData("Current", currentPosition)
            .addData("Distance", rangeSensor.distance)
            .addData("AP", adjustmentPower())
        telemetry.update()

        currentPosition > target
    }
    driveJob.cancel()
}.apply {
    context = MoveActionContext(Drive(data.distance))
}