package us.gotrobot.grbase.action

import kotlinx.coroutines.withTimeout
import us.gotrobot.grbase.robot.FeatureInstallContext

class MoveAction(
    private val block: suspend MoveActionScope.() -> Unit,
    var context: MoveActionContext = MoveActionContext()
) : AbstractAction() {

    override suspend fun run(context: FeatureInstallContext) {
        if (!disabled) {
            withTimeout(timeoutMillis) {
                MoveActionScope(context, this@MoveAction.context).also { block.invoke(it) }
            }
        }
    }

}

class MoveActionScope(
    installContext: FeatureInstallContext,
    val context: MoveActionContext
) : AbstractActionScope(installContext)

fun move(block: suspend MoveActionScope.() -> Unit): MoveAction = MoveAction(block)

