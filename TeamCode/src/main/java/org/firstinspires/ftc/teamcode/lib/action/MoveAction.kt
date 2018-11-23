@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.channels.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.DriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.RobotHeadingLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.RobotPositionLocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.math.abs

/**
 * That [ActionScope] that is used in the scope of a [MoveAction] block.
 */
class MoveActionScope(robot: Robot) : AbstractActionScope(robot)

/**
 * Provides an action block for a [Robot] to run and provided context specifically for moving the
 * robot.
 */
class MoveAction(private val block: suspend MoveActionScope.() -> Unit) : AbstractAction() {

    override suspend fun run(robot: Robot) {
        if (!disabled) {
            withTimeout(timeoutMillis) {
                val scope = MoveActionScope(robot)
                block.invoke(scope)
            }
        }
    }

}

/**
 * Returns a [MoveAction] with the provided [block].
 */
fun move(block: suspend MoveActionScope.() -> Unit): MoveAction = MoveAction(block)

/**
 * Returns an [Action] Drives linearly with the provided [power] for the provided [duration].
 */
@Deprecated("Usage of time based drives should be avoided.")
fun timeDrive(duration: Long, power: Double): MoveAction = move {
    val driveTrain = requestFeature(DriveTrain::class)
    driveTrain.setPower(power, 0.0)
    delay(duration)
    driveTrain.stopAllMotors()
}

/**
 * Returns an [Action] that turns linearly with the provided [power] for the provided [duration].
 */
@Deprecated("Usage of time turns drives should be avoided.")
fun timeTurn(duration: Long, power: Double): MoveAction = move {
    val driveTrain = requestFeature(DriveTrain::class)
    driveTrain.setHeadingPower(power)
    delay(duration)
    driveTrain.stopAllMotors()
}

/**
 * Takes an angle that is greater than 180 or less than -180 and maps it to an angle that is in that
 * range.
 */
private tailrec fun properHeading(heading: Double): Double = when {
    heading > 180 -> properHeading(heading - 360)
    heading < -180 -> properHeading(heading + 360)
    else -> heading
}

/**
 * Returns an [Action] that turns linearly with the provided [power] to [targetHeading].
 */
fun turnTo(targetHeading: Double, power: Double): MoveAction = move {
    val driveTrain = requestFeature(DriveTrain::class)
    val localizer = requestFeature(RobotHeadingLocalizer::class)
    val heading = localizer.heading.openSubscription()

    val initialHeading = heading.receive()
    if (initialHeading > targetHeading) {
        driveTrain.setHeadingPower(-abs(power))
        while (heading.receive() > targetHeading) {
            yield()
        }
    } else if (initialHeading < targetHeading) {
        driveTrain.setHeadingPower(abs(power))
        while (heading.receive() < targetHeading) {
            yield()
        }
    }

    heading.cancel()
    driveTrain.stopAllMotors()
}

/**
 * Returns an [Action] that turns linearly with the provided [power] to an angle relative to the
 * current location.
 */
fun turn(deltaHeading: Double, power: Double): MoveAction = move {
    val localizer = requestFeature(RobotHeadingLocalizer::class)
    val heading = localizer.heading.openSubscription()
    val initialHeading = heading.first()
    val targetHeading = properHeading(initialHeading + deltaHeading)
    perform(turnTo(targetHeading, power))
}

/**
 * Returns an [Action] that drives linearly with the provided [power] to the provided [deltaDistance].
 */
fun drive(deltaDistance: Long, power: Double): MoveAction = move {
    val driveTrain = requestFeature(DriveTrain::class)
    val localizer = requestFeature(RobotPositionLocalizer::class)

    val positionChannel = localizer.position.openSubscription()
    val initialPosition = positionChannel.receive().linearPosition
    val targetPosition = initialPosition + deltaDistance

    when {
        initialPosition < targetPosition -> {
            driveTrain.setPower(abs(power), 0.0)
            while (positionChannel.receive().linearPosition < targetPosition) {
                yield()
            }
        }

        initialPosition > targetPosition -> {
            driveTrain.setPower(-abs(power), 0.0)
            while (positionChannel.receive().linearPosition > targetPosition) {
                yield()
            }
        }
    }

    positionChannel.cancel()
    driveTrain.stopAllMotors()
}
