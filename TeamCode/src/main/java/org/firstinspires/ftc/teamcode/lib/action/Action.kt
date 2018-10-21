package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureKey
import org.firstinspires.ftc.teamcode.lib.robot.MissingRobotFeatureException
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.reflect.KClass

interface ActionScope : CoroutineScope {
    fun <F : Feature> requestFeature(featureKey: FeatureKey<F>): F
    fun <F : Feature> requestFeature(featureClass: KClass<F>): F
    fun perform(action: Action)
}

class ActionScopeImpl(
    private val robot: Robot,
    parentContext: CoroutineContext,
    job: Job
) : ActionScope {

    override val coroutineContext: CoroutineContext = parentContext + job

    override fun <F : Feature> requestFeature(featureKey: FeatureKey<F>): F {
        return robot[featureKey] ?: throw MissingRobotFeatureException()
    }

    override fun <F : Feature> requestFeature(featureClass: KClass<F>): F {
        return robot[featureClass] ?: throw MissingRobotFeatureException()
    }

    fun CoroutineScope.startAction(action: Action): Job = launch {
        val job = coroutineContext[Job]!!
        val scope = ActionScopeImpl(robot, coroutineContext, job)
        action.start(scope)
    }

    override fun perform(action: Action) {
        startAction(action)
    }

}

interface Action {
    suspend fun start(scope: ActionScope)
}
