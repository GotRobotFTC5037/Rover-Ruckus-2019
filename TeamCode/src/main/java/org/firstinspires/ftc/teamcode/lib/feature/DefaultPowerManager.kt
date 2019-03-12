package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.action.MoveAction
import org.firstinspires.ftc.teamcode.lib.action.NothingPowerManager
import org.firstinspires.ftc.teamcode.lib.action.PowerManager
import org.firstinspires.ftc.teamcode.lib.pipeline.PipelineContext
import org.firstinspires.ftc.teamcode.lib.robot.RobotFeatureInstallContext

class DefaultPowerManager(
    private val powerManager: PowerManager
) : Feature() {

    suspend fun interceptor(context: PipelineContext<Action, RobotFeatureInstallContext>) {
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
            context: RobotFeatureInstallContext,
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