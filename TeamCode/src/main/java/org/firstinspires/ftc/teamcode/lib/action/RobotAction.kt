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

open class RobotAction(private val actionBlock: RobotActionBlock) {

    // TODO: Provide a new, child coroutine scope to run in.
    internal open suspend fun run(robot: Robot, coroutineScope: CoroutineScope) {
        val context = Context(robot, coroutineScope)
        run(context)
    }

    internal suspend fun run(context: Context) {
        actionBlock(context)
    }

    open class Context(val robot: Robot, private val coroutineScope: CoroutineScope) {

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

    val positionChannel = localizer.newPositionChannel()

    driveTrain.setPower(power, 0.0)
    while (positionChannel.receive().linearPosition < distance) {
        yield()
    }
    positionChannel.cancel()
    driveTrain.stopAllMotors()
}

