package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.CoroutineScope
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureKey
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

interface ActionScope : CoroutineScope {
    fun <F : Feature> requestFeature(featureKey: FeatureKey<F>): F
    fun <F : Feature> requestFeature(featureClass: KClass<F>): F
    suspend fun perform(action: Action)
    suspend fun perform(actions: Sequence<Action>)
}

open class StandardActionScope(private val robot: Robot) : ActionScope {

    override val coroutineContext: CoroutineContext = robot.coroutineContext

    override fun <F : Feature> requestFeature(featureKey: FeatureKey<F>): F = robot[featureKey]

    override fun <F : Feature> requestFeature(featureClass: KClass<F>): F = robot[featureClass]

    override suspend fun perform(action: Action) {
        action.run(robot, coroutineContext)
    }

    override suspend fun perform(actions: Sequence<Action>) {
        for(action in actions) {
            action.run(robot, coroutineContext)
        }
    }

}
