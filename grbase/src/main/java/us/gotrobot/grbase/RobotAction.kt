package us.gotrobot.grbase

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.isActive
import kotlinx.coroutines.experimental.yield
import kotlin.math.abs

typealias Action = RobotAction
typealias MoveAction = RobotMoveAction

class IncompatibleRobotActionException(override val message: String?) : Exception()

interface RobotActions {
    fun wait(duration: Long): RobotAction
    fun sequence(actions: List<RobotAction>): RobotAction
    fun forever(action: RobotAction): RobotAction
}

interface RobotMoveActions {
    fun timeDrive(duration: Long, power: Double): MoveAction
    fun timeTurn(duration: Long, power: Double): MoveAction
    fun turnTo(heading: Double, power: Double): MoveAction
}

typealias RobotActionBlock = suspend RobotAction.Context.() -> Unit

open class RobotAction(private val actionBlock: RobotActionBlock) {

    internal open suspend fun run(robot: Robot, coroutineScope: CoroutineScope) {
        val context = Context(robot, coroutineScope)
        run(context)
    }

    internal suspend fun run(context: Context) {
        actionBlock(context)
    }

    open class Context(private val robot: Robot, private val coroutineScope: CoroutineScope) {

        val isActive: Boolean get() = coroutineScope.isActive

        fun <TFeature : Any> requiredFeature(feature: RobotFeatureDescriptor<TFeature>): TFeature =
            try {
                robot.feature(feature)
            } catch (e: MissingRobotFeatureException) {
                throw IncompatibleRobotActionException(
                    "Action requires feature '${feature.key.name}' to be installed."
                )
            }
    }

    companion object Builder : RobotActions {

        override fun wait(duration: Long) = RobotAction {
            delay(duration)
        }

        override fun sequence(actions: List<RobotAction>) = RobotAction {
            for (action in actions) {
                action.run(this)
            }
        }

        override fun forever(action: RobotAction) = RobotAction {
            while (isActive) {
                action.run(this)
            }
        }

    }
}

class RobotMoveAction(actionBlock: RobotActionBlock) : Action(actionBlock) {

    companion object Builder : RobotMoveActions {

        override fun timeDrive(duration: Long, power: Double) = MoveAction {
            val driveTrain = requiredFeature(RobotDriveTrain)
            driveTrain.setPower(1.0, 0.0)
            delay(duration)
            driveTrain.stopAllMotors()
        }

        override fun timeTurn(duration: Long, power: Double) = MoveAction {
            val driveTrain = requiredFeature(RobotDriveTrain)
            driveTrain.setHeadingPower(power)
            delay(duration)
            driveTrain.stopAllMotors()
        }

        override fun turnTo(heading: Double, power: Double) = MoveAction {
            val localizer = requiredFeature(RobotHeadingLocalizer)
            val driveTrain = requiredFeature(RobotDriveTrain)
            val currentHeading = localizer.headingChannel.receive()
            if (currentHeading > heading) {
                driveTrain.setHeadingPower(-abs(power))
                while(currentHeading > localizer.headingChannel.receive()) { yield() }
            } else if (currentHeading < heading) {
                driveTrain.setHeadingPower(abs(power))
                while(currentHeading < localizer.headingChannel.receive()) { yield() }
            }
            driveTrain.stopAllMotors()
        }

    }

}