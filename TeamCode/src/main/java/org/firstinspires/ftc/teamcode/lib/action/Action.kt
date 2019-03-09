package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureKey
import org.firstinspires.ftc.teamcode.lib.feature.FeatureSet
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * The scope that an [Action]'s block is run in.
 */
interface ActionScope {
    fun <F : Feature> getFeature(featureKey: FeatureKey<F>): F
    suspend fun perform(action: Action)
}

/**
 * An [ActionScope] that contains the basic functions used in an [Action] block.
 */
abstract class AbstractActionScope(
    private val featureSet: FeatureSet,
    parentContext: CoroutineContext = EmptyCoroutineContext
) : ActionScope, CoroutineScope {
    override val coroutineContext: CoroutineContext = parentContext + Job(parentContext[Job])
    override fun <F : Feature> getFeature(featureKey: FeatureKey<F>): F = featureSet[featureKey]
    override suspend fun perform(action: Action): Unit = action.run(featureSet)
}

/**
 * An [ActionScope] that is used in situations where no customization is needed.
 */
class StandardActionScope(
    featureSet: FeatureSet,
    parentContext: CoroutineContext
) : AbstractActionScope(featureSet, parentContext)

/**
 * Describes a block of work for the robot to do.
 */
interface Action {
    var disabled: Boolean
    var timeoutMillis: Long
    suspend fun run(featureSet: FeatureSet)
}

/**
 * An [Action] that contains the basic functions and properties to run an action.
 */
abstract class AbstractAction : Action {
    override var disabled: Boolean = false
    override var timeoutMillis: Long = 30_000 // Autonomous is 30 seconds.
}

/**
 * An [Action] that used in situations where no configuration is needed.
 */
private class StandardAction(private val block: suspend ActionScope.() -> Unit) : AbstractAction() {
    override suspend fun run(featureSet: FeatureSet) {
        if (!disabled) {
            withTimeout(timeoutMillis) {
                val scope = StandardActionScope(featureSet, coroutineContext)
                block.invoke(scope)
            }
        }
    }
}

/**
 * Returns a [StandardAction] with the provided [block].
 */
fun action(block: suspend ActionScope.() -> Unit): Action = StandardAction(block)

suspend fun Robot.perform(block: suspend ActionScope.() -> Unit) =
    perform(action(block).apply { this.timeoutMillis = Long.MAX_VALUE })

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

fun repeat(action: Action, times: Int): Action = action {
    kotlin.repeat(times) {
        perform(action)
    }
}

/**
 * Returns an action that waits [duration] in milliseconds.
 */
fun wait(duration: Long): Action = action { delay(duration) }
