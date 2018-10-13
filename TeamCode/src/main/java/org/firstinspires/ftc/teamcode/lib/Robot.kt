package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.runBlocking
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Reports a situation where a requested [RobotFeature] isn't available.
 */
class MissingRobotFeatureException(override val message: String? = "") : Exception()

/**
 * Installs [RobotFeature] and runs [RobotAction]. The core interface of GRBase.
 */
interface Robot {

    /**
     * A [HardwareMap] instance used for user convenience.
     */
    val hardwareMap: HardwareMap

    /**
     * Installs a [RobotFeature] into the robot.
     */
    fun <C : RobotFeatureConfiguration, F : Any> install(
        feature: RobotFeature<C, F>,
        configure: C.() -> Unit = {}
    )

    fun setupAndWaitForStart()

    /**
     * Returns the [RobotFeature] as described by the provided [RobotFeatureDescriptor].
     */
    fun <T : Any> feature(feature: RobotFeatureDescriptor<T>): T

    /**
     * Runs the provided [RobotAction].
     */
    fun runAction(action: RobotAction)
}

internal class RobotImpl(private val linearOpMode: LinearOpMode) : Robot, CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext get() = Dispatchers.Default + job

    override val hardwareMap: HardwareMap
        get() = linearOpMode.hardwareMap

    private val features = RobotFeatureSet()

    override fun <C : RobotFeatureConfiguration, F : Any> install(
        feature: RobotFeature<C, F>,
        configure: C.() -> Unit
    ) {
        val featureInstance = feature.install(this, configure)
        features.add(feature, featureInstance)
    }

    override fun <T : Any> feature(feature: RobotFeatureDescriptor<T>): T {
        return features.getOrNull(feature) ?: throw MissingRobotFeatureException()
    }

    private fun setup() {
        job = Job()
    }

    override fun setupAndWaitForStart() {
        setup()
        linearOpMode.waitForStart()
    }

    override fun runAction(action: RobotAction) = runBlocking {
        action.run(this@RobotImpl, this@RobotImpl)
    }

}

/**
 * Creates an instance of [Robot], installing the features requested within the [init] block.
 */
fun createRobot(linearOpMode: LinearOpMode, init: Robot.() -> Unit): Robot {
    val robot = RobotImpl(linearOpMode)
    robot.init()
    return robot
}
