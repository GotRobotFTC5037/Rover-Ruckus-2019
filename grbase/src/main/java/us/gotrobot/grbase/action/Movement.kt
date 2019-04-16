package us.gotrobot.grbase.action

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import us.gotrobot.grbase.feature.HeadingCorrection
import us.gotrobot.grbase.feature.TargetHeading
import us.gotrobot.grbase.feature.drivetrain.LinearDriveTrain
import us.gotrobot.grbase.feature.drivetrain.MecanumDriveTrain
import us.gotrobot.grbase.feature.drivetrain.RotationalDriveTrain
import us.gotrobot.grbase.feature.drivetrain.mecanumDriveTrain
import us.gotrobot.grbase.feature.getSingle

// TODO: Redesign this action to work on all types of drive trains.
fun linearDrive(distance: Double): MoveAction = move {
    val driveTrain = features.getSingle(LinearDriveTrain::class)
    val localizer = features[MecanumDriveTrain.Localizer]

    val linearPositionChannel = localizer.linearPosition()
    target = distance

    if (distance > 0.0) {
        while (isActive && distance > linearPositionChannel.receive()) {
            driveTrain.setLinearPower(power())
        }
    } else if (distance < 0.0) {
        while (isActive && distance < linearPositionChannel.receive()) {
            driveTrain.setLinearPower(power())
        }
    }

    linearPositionChannel.cancel()
    driveTrain.setLinearPower(0.0)
}

// TODO: Redesign this action to work on all types of drive trains.
fun lateralDrive(distance: Double): MoveAction = move {
    val driveTrain = features[MecanumDriveTrain]
    val localizer = features[MecanumDriveTrain.Localizer]

    val lateralPositionChannel = localizer.lateralPosition()
    target = distance

    if (distance > 0.0) {
        while (isActive && distance > lateralPositionChannel.receive()) {
            driveTrain.setLateralPower(power())
        }
    } else if (distance < 0.0) {
        while (isActive && distance < lateralPositionChannel.receive()) {
            driveTrain.setLateralPower(power())
        }
    }

    lateralPositionChannel.cancel()
    driveTrain.setLinearPower(0.0)
}

// TODO: Redesign this action to work on all types of drive trains.
fun turnTo(heading: Double): MoveAction = move {
    val driveTrain = features.getSingle(RotationalDriveTrain::class)
    val headingCorrection = features[HeadingCorrection].apply { enabled = false }
    val targetHeading = features[TargetHeading].apply { targetHeading = heading }

    val initialDelta = targetHeading.deltaFromHeading(heading)
    target = initialDelta

    if (initialDelta > 0) {
        while (isActive && targetHeading.deltaFromHeading(heading) > 0) {
            driveTrain.setRotationalPower(power())
        }
    } else if (initialDelta < 0) {
        while (isActive && targetHeading.deltaFromHeading(heading) < 0) {
            driveTrain.setRotationalPower(power())
        }
    }

    driveTrain.setRotationalPower(0.0)
    headingCorrection.enabled = true
}

fun driveForever(power: Double): MoveAction = move {
    val driveTrain = features.getSingle(LinearDriveTrain::class)
    while (isActive) {
        driveTrain.setLinearPower(power)
        yield()
    }
}

fun timeDrive(time: Long, power: Double) = move {
    mecanumDriveTrain.setLinearPower(power)
    delay(time)
    mecanumDriveTrain.stop()
}