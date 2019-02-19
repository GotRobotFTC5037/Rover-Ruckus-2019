package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.TargetHeading
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.DriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.power

@ExperimentalCoroutinesApi
fun turnTo(targetHeading: Double): MoveAction = move {
    when (val driveTrain = requestFeature(DriveTrain::class)) {
        is TankDriveTrain -> {
            val localizer = requestFeature(IMULocalizer)
            val headingChannel = localizer.newHeadingChannel()
            val adjustedTargetHeading = properHeading(targetHeading)
            val initialHeading = properHeading(headingChannel.receive())
            val driveTrainJob = launch {
                if (targetHeading > initialHeading) {
                    while (true) {
                        driveTrain.setMotorPowers(TankDriveTrain.MotorPowers(power(), -power()))
                        yield()
                    }
                } else if (targetHeading < initialHeading) {
                    while (true) {
                        driveTrain.setMotorPowers(
                            TankDriveTrain.MotorPowers(-power(), power()))
                        yield()
                    }
                }
            }.apply {
                invokeOnCompletion {
                    driveTrain.stop()
                }
            }
            if (adjustedTargetHeading > initialHeading) {
                headingChannel.first {
                    telemetry.addLine()
                        .addData("Target", adjustedTargetHeading)
                        .addData("Current", it)
                    telemetry.update()

                    adjustedTargetHeading <= it
                }
            } else if (adjustedTargetHeading < initialHeading) {
                headingChannel.first {
                    telemetry.addLine()
                        .addData("Target", adjustedTargetHeading)
                        .addData("Current", it)
                    telemetry.update()

                    adjustedTargetHeading >= it
                }
            }
            driveTrainJob.cancel()
        }
    }
}.apply {
    context = MoveActionContext(TurnTo(targetHeading))
}

fun turn(deltaHeading: Double): MoveAction = move {
    perform(turnTo(requestFeature(TargetHeading).targetHeading + deltaHeading))
}

fun timeTurn(time: Long, power: Double) = action {
    val driveTrain = requestFeature(TankDriveTrain)
    driveTrain.setMotorPowers(TankDriveTrain.MotorPowers(-power, power))
    delay(time)
    driveTrain.stop()
}
