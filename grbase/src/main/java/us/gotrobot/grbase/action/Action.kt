package us.gotrobot.grbase.action

import kotlinx.coroutines.CoroutineScope
import us.gotrobot.grbase.feature.Feature
import us.gotrobot.grbase.feature.FeatureKey
import us.gotrobot.grbase.robot.Robot
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass

typealias ActionBlock = suspend ActionScope.() -> Unit

class Action internal constructor(
    private val block: ActionBlock
) {

    var context: ActionContext = ActionContext()

    internal suspend fun run(robot: Robot) {
        val scope = ActionScope(robot, context, coroutineContext)
        block.invoke(scope)
    }

}

class ActionScope internal constructor(
    internal val robot: Robot,
    val context: ActionContext,
    private val parentContext: CoroutineContext = EmptyCoroutineContext
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = parentContext
}

fun <F : Feature> ActionScope.feature(key: FeatureKey<F>) = robot.features[key]
fun <F : Any> ActionScope.feature(clazz: KClass<F>) = robot.features.getAll(clazz).single()
suspend fun ActionScope.perform(action: Action) = robot.perform(action)
suspend fun Robot.perform(block: ActionBlock) = perform(action(block))

fun action(block: ActionBlock) = Action(block)

fun actionSequenceOf(vararg actions: Action) = action {
    for (action in actions) {
        perform(action)
    }
}.apply {
    context.add(ActionName("Action Sequence"))
}
