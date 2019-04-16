package us.gotrobot.grbase.action

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import us.gotrobot.grbase.feature.FeatureSet
import us.gotrobot.grbase.robot.FeatureInstallContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * The scope that an [Action]'s block is run in.
 */
interface ActionScope : CoroutineScope {
    val features: FeatureSet
    suspend fun perform(action: Action)
}

/**
 * An [ActionScope] that contains the basic functions used in an [Action] block.
 */
abstract class AbstractActionScope(
    private val context: FeatureInstallContext,
    parentContext: CoroutineContext = EmptyCoroutineContext
) : ActionScope {
    override val features: FeatureSet = context.features
    override val coroutineContext: CoroutineContext = parentContext + Job(parentContext[Job])
    override suspend fun perform(action: Action) {
        context.telemetry.log().add("[Action] Performing Action")
        context.actionPipeline.execute(action, context).run(context)
    }
}

/**
 * Describes a block of work for the robot to do.
 */
interface Action {
    var disabled: Boolean
    var timeoutMillis: Long
    suspend fun run(context: FeatureInstallContext)
}

/**
 * An [Action] that contains the basic functions and properties to run an action.
 */
abstract class AbstractAction : Action {
    override var disabled: Boolean = false
    override var timeoutMillis: Long = 30_000 // Autonomous is 30 seconds.
}
