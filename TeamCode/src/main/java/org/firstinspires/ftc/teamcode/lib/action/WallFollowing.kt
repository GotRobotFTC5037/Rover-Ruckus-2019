package org.firstinspires.ftc.teamcode.lib.action

import com.qualcomm.robotcore.hardware.DistanceSensor
import kotlinx.coroutines.channels.any
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.FeatureKey
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrainLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.sensor.RangeSensor
import org.firstinspires.ftc.teamcode.lib.power
import kotlin.math.abs

data class WallFollowingData(
    val distance: Double,
    val wallDistance: Double,
    val coefficient: Double,
    val rangeSensorKey: FeatureKey<RangeSensor>
)

fun wallFollowingDrive(data: WallFollowingData) = move {
    val driveTrain = requestFeature(TankDriveTrain)
    val localizer = requestFeature(TankDriveTrainLocalizer)
    val rangeSensor = requestFeature(data.rangeSensorKey)

    val positionChannel = localizer.newPositionChannel()

    val driveJob = launch {
        if (data.distance > 0.0) {
            while (true) {
                driveTrain.setMotorPowers(TankDriveTrain.MotorPowers(power(), power()))
                yield()
            }
        } else if (data.distance < 0.0) {
            while (true) {
                driveTrain.setMotorPowers(
                    TankDriveTrain.MotorPowers(-power(), -power())
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
        telemetry.update()

        currentPosition > target
    }
    driveJob.cancel()
}.apply {
    context = MoveActionContext(Drive(data.distance))
}