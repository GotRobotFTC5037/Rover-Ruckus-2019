package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.withTimeout

import org.firstinspires.ftc.teamcode.lib.robot.Robot
/**
 * Provides an action block for a [Robot] to run and provided context specifically for moving the
 * robot.
 */
open class MoveAction(
    private val block: suspend MoveActionScope.() -> Unit,
    var context: MoveActionContext = MoveActionContext(UnspecifiedMoveActionType)
) : AbstractAction() {

    override suspend fun run(robot: Robot) {
        if (!disabled) {
            withTimeout(timeoutMillis) {
                MoveActionScope(robot, context).also {
                    block.invoke(it)
                }
            }
        }
    }

}

/**
 * That [ActionScope] that is used in the scope of a [MoveAction] block.
 */
class MoveActionScope(robot: Robot, val context: MoveActionContext) : AbstractActionScope(robot)

/**
 * Returns a [MoveAction] that
 */
fun move(block: suspend MoveActionScope.() -> Unit): MoveAction = MoveAction(block)

//infix fun MoveAction.using(element: MoveActionPipeline.MoveActionPipelineElement): MoveAction {
//
//}
