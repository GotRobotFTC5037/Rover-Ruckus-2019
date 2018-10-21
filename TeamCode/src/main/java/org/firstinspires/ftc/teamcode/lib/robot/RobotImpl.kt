package org.firstinspires.ftc.teamcode.lib.robot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.experimental.*
import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.action.ActionScopeImpl
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.FeatureKey
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

private class RobotImpl(private val linearOpMode: LinearOpMode) : Robot, CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = newSingleThreadContext("robot") + job

    private val features: MutableMap<FeatureKey<*>, Feature> = mutableMapOf()

    override fun <TConfiguration : FeatureConfiguration, TFeature : Feature> install(
        feature: FeatureInstaller<TConfiguration, TFeature>,
        configuration: TConfiguration.() -> Unit
    ) {
        val featureInstance =
            feature.install(linearOpMode.hardwareMap, coroutineContext, configuration)
        features[feature] = featureInstance
    }

    @Suppress("UNCHECKED_CAST")
    override fun <F : Feature> get(key: FeatureKey<F>): F? {
        return features[key] as? F
    }

    @Suppress("UNCHECKED_CAST")
    override fun <F : Feature> get(featureClass: KClass<F>): F? =
        features.filter { it::class.isSubclassOf(featureClass) }
            .toList()
            .singleOrNull()?.second as? F


    override fun start() {
        job = Job()
    }

    fun CoroutineScope.startAction(action: Action) = launch {
        val robot = this@RobotImpl
        val context = this@RobotImpl.coroutineContext
        val job = coroutineContext[Job]!!
        val scope = ActionScopeImpl(robot, context, job)
        action.start(scope)
    }

    override fun perform(action: Action) = runBlocking {
        this@RobotImpl.startAction(action).join()
    }

}

fun createRobot(linearOpMode: LinearOpMode, configure: Robot.() -> Unit): Robot {
    linearOpMode.hardwareMap ?: throw PrematureRobotCreationException()
    return RobotImpl(linearOpMode).apply(configure)
}

/**
 * Reports a situation where a requested [Feature] isn't available.
 */
class MissingRobotFeatureException(override val message: String? = "") : RuntimeException()

/**
 * Reports a situation where a [Robot] is attempted to be created before the [LinearOpMode] instance
 * is ready.
 */
class PrematureRobotCreationException : RuntimeException() {
    override val message: String? =
        "Robots must only be instantiated after `runOpMode()` is executed."
}