package us.gotrobot.grbase.feature

import us.gotrobot.grbase.action.Action
import us.gotrobot.grbase.action.MoveAction
import us.gotrobot.grbase.action.NothingPowerManager
import us.gotrobot.grbase.action.PowerManager
import us.gotrobot.grbase.pipeline.PipelineContext
import us.gotrobot.grbase.robot.FeatureInstallContext

class DefaultPowerManager(
    private val powerManager: PowerManager
) : Feature() {

    suspend fun interceptor(context: PipelineContext<Action, FeatureInstallContext>) {
        val subject = context.subject
        if (subject is MoveAction) {
            if (!subject.context.contains(PowerManager)) {
                subject.context[PowerManager] = powerManager
                context.proceed()
            }
        }
    }

    companion object Installer : KeyedFeatureInstaller<DefaultPowerManager, Configuration>() {

        override val name: String = "Default Power Manager"

        override suspend fun install(
            context: FeatureInstallContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): DefaultPowerManager {
            val configuration = Configuration().apply(configure)
            val defaultPowerManager = DefaultPowerManager(configuration.powerManager)
            context.actionPipeline.intercept {
                defaultPowerManager.interceptor(this)
            }
            return defaultPowerManager
        }

    }

    class Configuration : FeatureConfiguration {
        var powerManager: PowerManager = NothingPowerManager()
    }

}