package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.ConstantPowerManager
import org.firstinspires.ftc.teamcode.lib.NothingPowerManager
import org.firstinspires.ftc.teamcode.lib.PipelineContext
import org.firstinspires.ftc.teamcode.lib.PowerManager
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.robot.Robot

class DefaultPowerManager(
    private val powerManagers: Map<MoveActionType, PowerManager>
) : Feature {

    fun interceptor(context: PipelineContext<Action, Robot>) {
        val action = context.subject
        if (action is MoveAction) {
            action.context[PowerManager] = when (action.context.type) {
                is Drive -> ConstantPowerManager(0.50)
                is TurnTo -> ConstantPowerManager(0.75)
                else -> NothingPowerManager
            }
        }
    }

    class Configuration : FeatureConfiguration {

        internal val powerManagers = mutableMapOf<MoveActionType, PowerManager>()

        infix fun MoveActionClause.uses(powerManager: PowerManager) {

        }

    }

    companion object Installer : FeatureInstaller<Configuration, DefaultPowerManager> {
        override fun install(
            robot: Robot,
            configure: Configuration.() -> Unit
        ): DefaultPowerManager {
            val configuration = Configuration().apply(configure)
            val defaults = DefaultPowerManager(configuration.powerManagers)
            robot.actionPipeline.intercept {
                defaults.interceptor(this)
            }
            return defaults
        }
    }

}
