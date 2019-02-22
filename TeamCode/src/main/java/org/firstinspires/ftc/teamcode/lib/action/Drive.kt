package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.any
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.DriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrainLocalizer
import org.firstinspires.ftc.teamcode.lib.power
import kotlin.math.abs

fun timeDrive(power: Double, duration: Long): MoveAction = move {
    val driveTrain = requestFeature(TankDriveTrain)
    driveTrain.setMotorPowers(TankDriveTrain.MotorPowers(power, power))
    delay(duration)
    driveTrain.stop()
}

fun drive(deltaDistance: Double): MoveAction = move {
    when (val driveTrain = requestFeature(DriveTrain::class)) {
        is TankDriveTrain -> {
            val localizer = requestFeature(TankDriveTrainLocalizer)
            val positionChannel = localizer.newPositionChannel()
            val driveTrainJob = launch {
                if (deltaDistance > 0.0) {
                    while (true) {
                        driveTrain.setMotorPowers(TankDriveTrain.MotorPowers(power(), power()))
                        yield()
                    }
                } else if (deltaDistance < 0.0) {
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
            @Suppress("EXPERIMENTAL_API_USAGE")
            positionChannel.any {
                val target = abs(deltaDistance)
                val currentPosition = abs(it.average)

                telemetry.addLine()
                    .addData("Target", deltaDistance)
                    .addData("Current", currentPosition)
                telemetry.update()

                currentPosition > target
            }
            driveTrainJob.cancel()
        }
    }
}.apply {
    context = MoveActionContext(Drive(deltaDistance))
}
