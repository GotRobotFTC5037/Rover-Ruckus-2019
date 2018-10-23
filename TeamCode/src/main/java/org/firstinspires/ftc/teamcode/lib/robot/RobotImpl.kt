package org.firstinspires.ftc.teamcode.lib.robot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.*
import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.action.ActionScope
import org.firstinspires.ftc.teamcode.lib.action.action
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.FeatureKey
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

private class RobotImpl(private val linearOpMode: LinearOpMode) : Robot, CoroutineScope {

    private val job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = newSingleThreadContext("robot") + job

    init {
        runBlocking {
            while (!linearOpMode.isStarted) {
                yield()
            }
        }
        launch {
            while (!linearOpMode.isStopRequested) {
                yield()
            }
            job.cancel()
        }
    }

    private val features: MutableMap<FeatureKey<*>, Feature> = mutableMapOf()

    private val activeActions: MutableList<Job> = mutableListOf()

    override fun <TConfiguration : FeatureConfiguration, TFeature : Feature> install(
        feature: FeatureInstaller<TConfiguration, TFeature>,
        configuration: TConfiguration.() -> Unit
    ) {
        val featureInstance =
            feature.install(linearOpMode.hardwareMap, coroutineContext, configuration)
        features[feature] = featureInstance
    }

    override fun contains(key: FeatureKey<*>): Boolean = key in features

    override fun contains(featureClass: KClass<Feature>): Boolean =
        features.any { it.value::class.isSubclassOf(featureClass) }

    @Suppress("UNCHECKED_CAST")
    override fun <F : Feature> get(key: FeatureKey<F>): F? = features[key] as? F


    @Suppress("UNCHECKED_CAST")
    override fun <F : Feature> get(featureClass: KClass<F>): F? =
        features.filter { it.value::class.isSubclassOf(featureClass) }
            .toList()
            .singleOrNull()
            ?.second as? F

    override fun perform(action: Action) = runBlocking {
        action.run(this@RobotImpl, coroutineContext)
    }

    override fun waitForActionsToComplete() = runBlocking { activeActions.forEach { it.join() } }

}

fun Robot.perform(block: suspend ActionScope.() -> Unit) {
    val action = action(block)
    perform(action)
}

fun robot(linearOpMode: LinearOpMode, configure: Robot.() -> Unit): Robot {
    linearOpMode.hardwareMap ?: throw PrematureRobotCreationException()
    return RobotImpl(linearOpMode).apply(configure)
}

/**
 * Reports a situation where a [Robot] is attempted to be created before the [LinearOpMode] instance
 * is ready.
 */
class PrematureRobotCreationException : RuntimeException() {
    override val message: String? =
        "Robots must only be instantiated after `runOpMode()` is executed."
}