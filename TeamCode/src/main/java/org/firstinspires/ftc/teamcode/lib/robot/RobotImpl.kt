package org.firstinspires.ftc.teamcode.lib.robot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.action.ActionPipeline
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.FeatureKey
import org.firstinspires.ftc.teamcode.lib.util.cancelAndJoin
import org.firstinspires.ftc.teamcode.lib.util.delayUntilStart
import org.firstinspires.ftc.teamcode.lib.util.delayUntilStop
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

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

    override fun <TConfiguration : FeatureConfiguration, TFeature : Feature> install(
        feature: FeatureInstaller<TConfiguration, TFeature>,
        key: FeatureKey<TFeature>,
        configuration: TConfiguration.() -> Unit
    ) {
        telemetry.log().add("Installing ${feature::class.qualifiedName}")
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
        telemetry.update()
        return@coroutineScope
    }

    override fun performBlocking(action: Action) = runBlocking {
        perform(action)
    }

}

suspend fun robot(
    linearOpMode: LinearOpMode,
    opModeScope: CoroutineScope,
    configure: Robot.() -> Unit
): Robot {
    linearOpMode.hardwareMap ?: throw PrematureRobotCreationException()

    linearOpMode.telemetry.log().displayOrder = Telemetry.Log.DisplayOrder.NEWEST_FIRST
    linearOpMode.telemetry.log().capacity = 5

    linearOpMode.telemetry.log().add("Setting up robot...")
    val robot = RobotImpl(linearOpMode, opModeScope).apply(configure)

    linearOpMode.telemetry.log().add("Waiting for start...")
    linearOpMode.delayUntilStart()

    robot.launch {
        linearOpMode.delayUntilStop()
        robot.cancel()
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