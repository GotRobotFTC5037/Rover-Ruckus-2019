package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.TargetHeading
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.DriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.power

fun turnTo(targetHeading: Double): MoveAction = move {
    tailrec fun properHeading(heading: Double): Double = when {
        heading >= 180.0 -> properHeading(heading - 360)
        heading < -180.0 -> properHeading(heading + 360)
        else -> heading
    }
    when (val driveTrain = requestFeature(DriveTrain::class)) {
        is TankDriveTrain -> {
            val localizer = requestFeature(IMULocalizer)
            val headingChannel = localizer.newHeadingChannel()
            val adjustedTargetHeading = properHeading(targetHeading)
            val initialHeading = properHeading(headingChannel.receive())
            val driveTrainJob = launch {
                while (true) {
                    driveTrain.setMotorPowers(-power(), power())
                    yield()
                }
            }.apply {
                invokeOnCompletion {
                    driveTrain.stop()
                }
            }
            if (adjustedTargetHeading > initialHeading) {
                headingChannel.first { adjustedTargetHeading <= it }
            } else if (adjustedTargetHeading < initialHeading) {
                headingChannel.first { adjustedTargetHeading >= it }
            }
            driveTrainJob.cancel()
        }
    }
}

fun turn(deltaHeading: Double): MoveAction = move {
    perform(turnTo(requestFeature(TargetHeading).targetHeading + deltaHeading))
}
