package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.experimental.delay


class StandardAction(private val block: suspend ActionScope.() -> Unit) : Action {
    override suspend fun start(scope: ActionScope) {
        block(scope)
    }
}

/**
 * Returns a robot action
 */
fun perform(block: suspend ActionScope.() -> Unit): Action {
    return StandardAction(block)
}

/**
 * Returns a [Action] that runs the provided [actions] in order.
 */
fun perform(actionSequence: Sequence<Action>): Action = StandardAction {
    for (action in actionSequence) {
        perform(action)
    }
}

/**
 * Returns a [Action] that waits for [duration] milliseconds.
 */
fun wait(duration: Long): Action = perform {
    delay(duration)
}
