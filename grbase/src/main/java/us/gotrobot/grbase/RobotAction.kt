package us.gotrobot.grbase

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.isActive

typealias Action = RobotAction
typealias MoveAction = RobotMoveAction

class IncompatibleRobotActionException(override val message: String?) : Exception()

interface RobotActions {
    fun wait(duration: Long): RobotAction
    fun sequence(actions: List<RobotAction>): RobotAction
    fun forever(action: RobotAction): RobotAction
}

interface RobotMoveActions {
    fun timeDrive(duration: Long, power: Double, init: MoveAction.() -> Unit = {}): MoveAction
    fun timeTurn(duration: Long, power: Double, init: MoveAction.() -> Unit = {}): MoveAction
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

        var name = "(unnamed)"

        val isActive: Boolean get() = coroutineScope.isActive

        fun <C : Component> requiredComponent(component: ComponentInstaller<*, C>): C = try {
            robot.getComponent(component)
        } catch (e: MissingRobotComponentExeption) {
            throw IncompatibleRobotActionException(
                "The action with name '$name' requires the component '${component.key.name}' " +
                        "to be installed in order to run."
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

        override fun timeDrive(duration: Long, power: Double, init: MoveAction.() -> Unit) =
            MoveAction {
            val driveTrain = requiredComponent(TankDrive)
            driveTrain.setMotorPowers(1.0, 1.0)
            delay(duration)
            driveTrain.setMotorPowers(0.0, 0.0)
        }.apply(init)

        override fun timeTurn(duration: Long, power: Double, init: MoveAction.() -> Unit) =
            MoveAction {
            val driveTrain = requiredComponent(TankDrive)
            driveTrain.setMotorPowers(1.0, 1.0)
            delay(duration)
            driveTrain.setMotorPowers(0.0, 0.0)
        }.apply(init)

    }

}