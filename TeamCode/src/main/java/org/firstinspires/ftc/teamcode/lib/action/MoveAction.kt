package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.DriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.RobotHeadingLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.localizer.RobotPositionLocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.math.abs

/**
 * Provides an action block for a [Robot] to run and provided context specifically for moving the
 * robot.
 */
typealias MoveAction = Action

fun move(block: suspend ActionScope.() -> Unit): MoveAction = action(block)

/**
 * Returns an [Action] Drives linearly with the provided [power] for the provided [duration].
 */
fun timeDrive(duration: Long, power: Double): MoveAction = move {
    val driveTrain = requestFeature(DriveTrain::class)
    driveTrain.setPower(power, 0.0)
    delay(duration)
    driveTrain.stopAllMotors()
}

/**
 * Returns an [Action] that turns linearly with the provided [power] for the provided [duration].
 */
fun timeTurn(duration: Long, power: Double): MoveAction = move {
    val driveTrain = requestFeature(DriveTrain::class)
    driveTrain.setHeadingPower(power)
    delay(duration)
    driveTrain.stopAllMotors()
}

/**
 * Returns an [Action] that turns linearly with the provided [power] to [targetHeading].
 */
fun turnTo(targetHeading: Double, power: Double): MoveAction = move {
    val driveTrain = requestFeature(DriveTrain::class)
    val localizer = requestFeature(RobotHeadingLocalizer::class)

    val headingChannel = localizer.heading.openSubscription()

    val initialHeading = headingChannel.receive()
    if (initialHeading > targetHeading) {
        driveTrain.setHeadingPower(-abs(power))
        while (targetHeading < headingChannel.receive()) {
            yield()
        }
    } else if (initialHeading < targetHeading) {
        driveTrain.setHeadingPower(abs(power))
        while (targetHeading > headingChannel.receive()) {
            yield()
        }
    }
    headingChannel.cancel()
    driveTrain.stopAllMotors()
}

/**
 * Returns an [Action] that turns linearly with the provided [power] to an angle relative to the
 * current location.
 */
fun turn(deltaHeading: Double, power: Double): MoveAction = move {
    val localizer = requestFeature(RobotHeadingLocalizer::class)

    val headingChannel = localizer.heading.openSubscription()

    val initialHeading = headingChannel.receive()
    val rawAbsoluteDesiredHeading = initialHeading + deltaHeading

    tailrec fun calculateProperHeading(heading: Double): Double = when {
        heading > 180 -> calculateProperHeading(heading - 360)
        heading < -180 -> calculateProperHeading(heading + 360)
        else -> heading
    }

    headingChannel.cancel()
    perform(turnTo(calculateProperHeading(rawAbsoluteDesiredHeading), power))
}

/**
 * Returns an [Action] that drives linearly with the provided [power] to the provided distance.
 */
fun driveTo(distance: Long, power: Double): MoveAction = move {
    val driveTrain = requestFeature(DriveTrain::class)
    val localizer = requestFeature(RobotPositionLocalizer::class)

    val positionChannel = localizer.position.openSubscription()

    driveTrain.setPower(power, 0.0)
    while (positionChannel.receive().linearPosition < distance) {
        yield()
    }
    positionChannel.cancel()
    driveTrain.stopAllMotors()
}