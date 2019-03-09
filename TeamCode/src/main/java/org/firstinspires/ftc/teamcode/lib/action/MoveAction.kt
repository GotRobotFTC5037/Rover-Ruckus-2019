package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.withTimeout
import org.firstinspires.ftc.teamcode.lib.feature.FeatureSet

class MoveAction(
    private val block: suspend MoveActionScope.() -> Unit,
    var context: MoveActionContext = MoveActionContext()
) : AbstractAction() {

    override suspend fun run(featureSet: FeatureSet) {
        if (!disabled) {
            withTimeout(timeoutMillis) {
                MoveActionScope(featureSet, context).also { block.invoke(it) }
            }
        }
    }

}

class MoveActionScope(
    featureSet: FeatureSet,
    val context: MoveActionContext
) : AbstractActionScope(featureSet)

fun move(block: suspend MoveActionScope.() -> Unit): MoveAction = MoveAction(block)

