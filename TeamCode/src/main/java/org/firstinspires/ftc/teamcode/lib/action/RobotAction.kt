package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.isActive
import kotlinx.coroutines.experimental.yield
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeature
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.RobotDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.RobotHeadingLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.localizer.RobotPositionLocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.RobotImpl
import org.firstinspires.ftc.teamcode.lib.util.waitFor
import kotlin.math.abs
import kotlin.reflect.KClass

typealias RobotActionBlock = suspend RobotAction.Context.() -> Unit

/**
 * Provides an action block for a [Robot] to run with the necessary context and properties for
 * customization. Every [RobotAction] should be run using [Robot.runAction] because it provides the
 * necessary structure to ensure actions are run within the correct time and scopes.
 */
open class RobotAction(private val actionBlock: RobotActionBlock) {

    /**
     * Runs [actionBlock] and and provides a new [Context] with [robot] and [coroutineScope]. This
     * function should only be run within an instance of [Robot] or another [RobotActionBlock]
     */
    internal open suspend fun run(robot: Robot, coroutineScope: CoroutineScope) {
        // TODO: Provide a new, child coroutine scope to run in.
        val context = Context(robot, coroutineScope)
        run(context)
    }

    /**
     * Runs [actionBlock] and and provides it [context] as its context. This function should only
     * be run within an instance of [Robot] or another [RobotActionBlock]
     */
    internal suspend fun run(context: Context) {
        actionBlock(context)
    }

    /**
     * Provides a [RobotActionBlock] with data and functions necessary to run actions.
     */
    open class Context(val robot: Robot, private val coroutineScope: CoroutineScope) {

        /**
         * Tells [RobotActionBlock] if it is active. This should be used in order to make a
         * [RobotAction] cancelable which is especially important for long running, robot moving
         * tasks.
         */
        val isActive: Boolean get() = coroutineScope.isActive

        fun <TFeature : RobotFeature> requiredFeature(featureClass: KClass<TFeature>): TFeature =
                (robot as RobotImpl).feature(featureClass)

        inline fun <reified TFeature : RobotFeature> requiredFeature(installer: RobotFeatureInstaller<*, TFeature>): TFeature =
                (robot as RobotImpl).feature(TFeature::class)

    }

}

fun wait(duration: Long): RobotAction = RobotAction {
    delay(duration)
}

fun sequence(vararg actions: RobotAction): RobotAction = RobotAction {
    for (action in actions) {
        action.run(this)
    }
}

fun forever(action: RobotAction): RobotAction = RobotAction {
    while (isActive) {
        action.run(this)
    }
}

class RobotMoveAction(actionBlock: RobotActionBlock) : RobotAction(actionBlock)

fun timeDrive(duration: Long, power: Double): RobotMoveAction = RobotMoveAction {
    val driveTrain = requiredFeature(RobotDriveTrain::class)
    driveTrain.setPower(power, 0.0)
    delay(duration)
    driveTrain.stopAllMotors()
}

fun timeTurn(duration: Long, power: Double): RobotMoveAction = RobotMoveAction {
    val driveTrain = requiredFeature(RobotDriveTrain::class)
    driveTrain.setHeadingPower(power)
    delay(duration)
    driveTrain.stopAllMotors()
}

fun turnTo(targetHeading: Double, power: Double): RobotMoveAction =
        RobotMoveAction {
            val driveTrain = requiredFeature(RobotDriveTrain::class)
            val localizer = requiredFeature(RobotHeadingLocalizer::class)
            waitFor(localizer::isReady)

            val headingChannel = localizer.newHeadingChannel()

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

fun turn(deltaHeading: Double, power: Double): RobotMoveAction = RobotMoveAction {
    val localizer = requiredFeature(RobotHeadingLocalizer::class)

    val headingChannel = localizer.newHeadingChannel()

    val initialHeading = headingChannel.receive()
    val rawAbsoluteDesiredHeading = initialHeading + deltaHeading

    tailrec fun calculateProperHeading(heading: Double): Double = when {
        heading > 180 -> calculateProperHeading(heading - 360)
        heading < -180 -> calculateProperHeading(heading + 360)
        else -> heading
    }

    turnTo(calculateProperHeading(rawAbsoluteDesiredHeading), power).run(this)
}

fun driveTo(distance: Long, power: Double): RobotMoveAction = RobotMoveAction {
    val driveTrain = requiredFeature(RobotDriveTrain::class)
    val localizer = requiredFeature(RobotPositionLocalizer::class)

    val positionChannel = localizer.positionChannel

    driveTrain.setPower(power, 0.0)
    while (positionChannel.receive().linearPosition < distance) {
        yield()
    }
    driveTrain.stopAllMotors()
}

