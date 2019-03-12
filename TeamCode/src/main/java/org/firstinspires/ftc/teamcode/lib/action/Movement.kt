package org.firstinspires.ftc.teamcode.lib.action

import com.acmerobotics.roadrunner.trajectory.Trajectory
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.TargetHeading
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.MecanumDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.RotationalDriveTrain

// TODO: Redesign this action to work on all types of drive trains.
fun followTrajectory(trajectory: Trajectory) = move {
    val roadRunner = getFeature(MecanumDriveTrain.RoadRunnerExtension)
    roadRunner.trajectory = trajectory
    while (isActive && roadRunner.isFollowing) {
        roadRunner.updateMotorPowers()
        yield()
    }
}

// TODO: Redesign this action to work on all types of drive trains.
fun linearDrive(distance: Double): MoveAction = move {
    val driveTrain = getFeature(MecanumDriveTrain)
}

// TODO: Redesign this action to work on all types of drive trains.
fun lateralDrive(distance: Double): MoveAction = move {
    val driveTrain = getFeature(MecanumDriveTrain)
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
