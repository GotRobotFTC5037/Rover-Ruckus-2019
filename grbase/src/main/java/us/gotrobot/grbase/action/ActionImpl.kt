package us.gotrobot.grbase.action

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import us.gotrobot.grbase.feature.Feature
import us.gotrobot.grbase.feature.FeatureKey
import us.gotrobot.grbase.robot.FeatureInstallContext
import us.gotrobot.grbase.robot.Robot

/**
 * An [ActionScope] that is used in situations where no customization is needed.
 */
class StandardActionScope(
    context: FeatureInstallContext
) : AbstractActionScope(context)

/**
 * An [Action] that used in situations where no configuration is needed.
 */
private class StandardAction(
    private val block: suspend ActionScope.() -> Unit
) : AbstractAction() {

    override suspend fun run(context: FeatureInstallContext) {
        if (!disabled) {
            withTimeout(timeoutMillis) {
                StandardActionScope(context).also { block.invoke(it) }
            }
        }
    }

}

/**
 *
 */
fun <T : Feature> ActionScope.feature(key: FeatureKey<T>) = features[key]

/**
 * Returns a [StandardAction] with the provided [block].
 */
fun action(block: suspend ActionScope.() -> Unit): Action = StandardAction(block)

/**
 *
 */
suspend fun Robot.perform(block: suspend ActionScope.() -> Unit) =
    perform(action(block).apply { this.timeoutMillis = Long.MAX_VALUE })

/**
 *
 */
suspend fun Robot.performAsync(action: Action) =
    perform { launch { this@performAsync.perform(action) } }

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
 *
 */
fun repeat(action: Action, times: Int): Action =
    action { kotlin.repeat(times) { perform(action) } }

/**
 * Returns an action that waits [duration] in milliseconds.
 */
fun wait(duration: Long): Action = action { delay(duration) }