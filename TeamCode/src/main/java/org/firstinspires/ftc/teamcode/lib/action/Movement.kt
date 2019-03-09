package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.isActive
import org.firstinspires.ftc.teamcode.lib.feature.TargetHeading
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.MecanumDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.RotationalDriveTrain

// TODO: Redesign this action to work on all types of drive trains.
fun linearDrive(distance: Double): MoveAction = move {

}

// TODO: Redesign this action to work on all types of drive trains.
fun lateralDrive(distance: Double): MoveAction = move {
    TODO("Lateral Drive")
}

// TODO: Redesign this action to work on all types of drive trains.
fun turnTo(heading: Double): MoveAction = move {
    val driveTrain: RotationalDriveTrain = getFeature(MecanumDriveTrain)
    val targetHeading = getFeature(TargetHeading).apply { targetHeading = heading }
    val initialDelta = targetHeading.deltaFromHeading(heading)
    if (initialDelta > 0) {
        while (isActive && targetHeading.deltaFromHeading(heading) > 0) {
            driveTrain.setRotationalPower(power())
        }
    } else if (initialDelta < 0) {
        while (isActive && targetHeading.deltaFromHeading(heading) < 0) {
            driveTrain.setRotationalPower(-power())
        }
    }
    driveTrain.setRotationalPower(0.0)
}
