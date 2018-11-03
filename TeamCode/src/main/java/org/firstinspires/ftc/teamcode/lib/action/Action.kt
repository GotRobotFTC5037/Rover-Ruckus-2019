package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.delay
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.CoroutineContext

abstract class Action {
    open var name: String = "[Unnamed]"
    abstract suspend fun run(robot: Robot, parentContext: CoroutineContext)
}

private class ActionImpl(private val block: suspend ActionScope.() -> Unit) : Action() {
    override suspend fun run(robot: Robot, parentContext: CoroutineContext) {
        val scope: ActionScope = StandardActionScope(robot)
        scope.block()
    }
}

infix fun Action.then(action: Action): Action = actionSequenceOf(this, action)

fun action(block: suspend ActionScope.() -> Unit): Action = ActionImpl(block)

fun actionSequenceOf(vararg actions: Action): Action = action {
    for (action in actions) {
        perform(action)
    }
}

fun wait(duration: Long): Action = action {
    delay(duration)
}
