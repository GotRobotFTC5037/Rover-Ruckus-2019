package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.PipelineContext
import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.action.MoveAction
import org.firstinspires.ftc.teamcode.lib.action.MoveActionKeys
import org.firstinspires.ftc.teamcode.lib.action.MoveActionType
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.PowerManager

class MoveActionDefaults(
    private val powerManagers: Map<MoveActionType, PowerManager>
) : Feature {

    fun interceptor(context: PipelineContext<Action, Robot>) {
        val action = context.subject
        if (action is MoveAction) {
            for (entry in powerManagers) {
                if (action.attributes[MoveActionKeys.Type] == entry.key) {
                    action.attributes[MoveActionKeys.PowerManager] = entry.value
                }
            }
        }
    }

    class Configuration : FeatureConfiguration {

        internal val powerManagers = mutableMapOf<MoveActionType, PowerManager>()

        fun defaultPowerManager(type: MoveActionType, powerManager: PowerManager) {
            powerManagers[type] = powerManager
        }

    }

    companion object Installer : FeatureInstaller<Configuration, MoveActionDefaults> {
        override fun install(
            robot: Robot,
            configure: Configuration.() -> Unit
        ): MoveActionDefaults {
            val configuration = Configuration().apply(configure)
            val defaults = MoveActionDefaults(configuration.powerManagers)
            robot.actionPipeline.intercept {
                defaults.interceptor(this)
            }
            return defaults
        }
    }

}
