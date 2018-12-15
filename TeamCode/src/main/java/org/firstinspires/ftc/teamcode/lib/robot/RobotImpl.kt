package org.firstinspires.ftc.teamcode.lib.robot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.*
import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.action.ActionPipeline
import org.firstinspires.ftc.teamcode.lib.action.ActionScope
import org.firstinspires.ftc.teamcode.lib.action.action
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.FeatureKey
import org.firstinspires.ftc.teamcode.lib.util.delayUntilStart
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 *
 */
private class RobotImpl(
    override val linearOpMode: LinearOpMode,
    override val opmodeScope: CoroutineScope
) : Robot {

    private val job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val features: MutableMap<FeatureKey<*>, Feature> = mutableMapOf()

    /**
     * The pipeline that actions go though before they are performed.
     */
    override val actionPipeline: ActionPipeline = ActionPipeline()

    /**
     * Installs a feature onto the robot.
     */
    override fun <TConfiguration : FeatureConfiguration, TFeature : Feature> install(
        feature: FeatureInstaller<TConfiguration, TFeature>,
        configuration: TConfiguration.() -> Unit
    ) {
        val featureInstance = feature.install(this, configuration)
        features[feature] = featureInstance
    }

    override fun <TConfiguration : FeatureConfiguration, TFeature : Feature> install(
        feature: FeatureInstaller<TConfiguration, TFeature>,
        key: FeatureKey<TFeature>,
        configuration: TConfiguration.() -> Unit
    ) {
        val featureInstance = feature.install(this, configuration)
        features[key] = featureInstance
    }

    /**
     * Checks if a feature with the provided [key] is installed on the robot.
     */
    override fun contains(key: FeatureKey<*>): Boolean = key in features

    /**
     * Checks if any feature of the Feature class are installed on the robot.
     */
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

    override suspend fun perform(action: Action) = coroutineScope {
        actionPipeline.execute(action, this@RobotImpl)
        action.run(this@RobotImpl)
    }

    override fun performBlocking(action: Action) = runBlocking {
        perform(action)
    }

}

suspend fun Robot.perform(block: suspend ActionScope.() -> Unit) {
    val action = action(block)
    perform(action)
}

suspend fun robot(linearOpMode: LinearOpMode, coroutineScope: CoroutineScope, configure: Robot.() -> Unit): Robot {
    linearOpMode.hardwareMap ?: throw PrematureRobotCreationException()

    linearOpMode.telemetry.log().add("Setting up robot...")
    val robot = RobotImpl(linearOpMode, coroutineScope).apply(configure)

    linearOpMode.telemetry.log().add("Waiting for start...")
    linearOpMode.delayUntilStart()

    robot.launch {
        while(!linearOpMode.isStopRequested) {
            yield()
        }
        robot.coroutineContext[Job]!!.cancel()
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