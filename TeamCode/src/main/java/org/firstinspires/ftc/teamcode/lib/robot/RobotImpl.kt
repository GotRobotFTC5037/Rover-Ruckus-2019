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
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

private class RobotImpl(override val linearOpMode: LinearOpMode) : Robot {

    private val job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    init {
        GlobalScope.launch {
            while (!linearOpMode.isStopRequested) {
                yield()
            }
            job.cancel()
        }
    }

    private val features: MutableMap<FeatureKey<*>, Feature> = mutableMapOf()

    override fun <TConfiguration : FeatureConfiguration, TFeature : Feature> install(
        feature: FeatureInstaller<TConfiguration, TFeature>,
        configuration: TConfiguration.() -> Unit
    ) {
        val featureInstance = feature.install(this, configuration)
        features[feature] = featureInstance
    }

    override fun contains(key: FeatureKey<*>): Boolean = key in features

    override fun contains(featureClass: KClass<Feature>): Boolean =
        features.any { it.value::class.isSubclassOf(featureClass) }

    @Suppress("UNCHECKED_CAST")
    override fun <F : Feature> get(key: FeatureKey<F>): F =
        features[key] as? F ?: throw MissingRobotFeatureException()

    @Suppress("UNCHECKED_CAST")
    override fun <F : Feature> get(featureClass: KClass<F>): F =
        features.filter { it.value::class.isSubclassOf(featureClass) }
            .toList()
            .singleOrNull()
            ?.second as? F ?: throw MissingRobotFeatureException()

    override fun perform(action: Action) = runBlocking {
        action.run(this@RobotImpl, coroutineContext)
    }

}

fun Robot.perform(block: suspend ActionScope.() -> Unit) {
    val action = action(block)
    perform(action)
}

fun robot(linearOpMode: LinearOpMode, configure: Robot.() -> Unit): Robot {
    linearOpMode.hardwareMap ?: throw PrematureRobotCreationException()
    val robot = RobotImpl(linearOpMode).apply(configure)
    linearOpMode.telemetry.log().add("Waiting for start...")
    while (!linearOpMode.isStarted) {
        linearOpMode.idle()
    }
    return robot
}

/**
 * Reports a situation where a [Robot] is attempted to be created before the [LinearOpMode] instance
 * is ready.
 */
class PrematureRobotCreationException : RuntimeException() {
    override val message: String? =
        "Robots must only be instantiated after `runOpMode()` is executed."
}