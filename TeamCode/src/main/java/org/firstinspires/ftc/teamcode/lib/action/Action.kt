package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureKey
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

/**
 * The scope that an [Action]'s block is run in.
 */
interface ActionScope : CoroutineScope {
    fun <F : Feature> requestFeature(featureKey: FeatureKey<F>): F
    fun <F : Feature> requestFeature(featureClass: KClass<F>): F
    suspend fun perform(action: Action)
    suspend fun perform(actions: Sequence<Action>)
}

/**
 * An [ActionScope] that contains the basic functions used in an [Action] block.
 */
abstract class AbstractActionScope(private val robot: Robot) : ActionScope {

    override val coroutineContext: CoroutineContext = robot.coroutineContext

    override fun <F : Feature> requestFeature(featureKey: FeatureKey<F>): F = robot[featureKey]

    override fun <F : Feature> requestFeature(featureClass: KClass<F>): F = robot[featureClass]

    override suspend fun perform(action: Action) {
        action.run(robot)
    }

    override suspend fun perform(actions: Sequence<Action>): Unit =
        actions.forEach { action -> action.run(robot) }

}

/**
 * An [ActionScope] that is used in situations where no customization is needed.
 */
class StandardActionScope(robot: Robot) : AbstractActionScope(robot)

/**
 * Describes a block of work for the robot to do.
 */
interface Action {
    var disabled: Boolean
    var timeoutMillis: Long
    suspend fun run(robot: Robot)
}

/**
 * An [Action] that contains the basic functions and properties to run an action.
 */
abstract class AbstractAction : Action {
    override var disabled: Boolean = false
    override var timeoutMillis: Long = Long.MAX_VALUE
}

/**
 * An [Action] that used in situations where no configuration is needed.
 */
private class StandardAction(private val block: suspend ActionScope.() -> Unit) : AbstractAction() {

    override suspend fun run(robot: Robot) {
        if (!disabled) {
            withTimeout(timeoutMillis) {
                val scope = StandardActionScope(robot)
                block.invoke(scope)
            }
        }
    }

}

/**
 * Returns a [StandardAction] with the provided [block].
 */
fun action(block: suspend ActionScope.() -> Unit): Action = StandardAction(block)

/**
 * Returns an action that performs the current action and the provided action in sequence.
 */
infix fun Action.then(action: Action): Action = actionSequenceOf(this, action)

/**
 * Returns an action that performs [actions] in sequence.
 */
fun actionSequenceOf(vararg actions: Action): Action = action {
    for (action in actions) {
        perform(action)
    }
}

/**
 * Returns an action that waits [duration] in milliseconds.
 */
fun wait(duration: Long): Action = action { delay(duration) }
