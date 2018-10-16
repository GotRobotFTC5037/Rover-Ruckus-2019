package org.firstinspires.ftc.teamcode.lib.robot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.runBlocking
import org.firstinspires.ftc.teamcode.lib.action.RobotAction
import org.firstinspires.ftc.teamcode.lib.action.RobotActionBlock
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeature
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeatureSet
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.reflect.KClass

/**
 * Installs [RobotFeature] and runs [RobotAction]. The core interface of GRBase.
 */
interface Robot {

    /**
     * A [HardwareMap] instance used for user convenience.
     */
    val hardwareMap: HardwareMap

    /**
     * Installs a robot feature using [featureInstaller] and configures it using [configuration].
     */
    fun <TConfiguration : RobotFeatureConfiguration, TFeature : RobotFeature> install(
        featureInstaller: RobotFeatureInstaller<TConfiguration, TFeature>,
        configuration: TConfiguration.() -> Unit = {}
    )

    fun <TFeature : RobotFeature> feature(featureClass: KClass<TFeature>): TFeature

    /**
     * Sets up the robot, waits for the opmode to start and then starts the robot.
     */
    fun setupAndWaitForStart()

    /**
     * Runs the provided [RobotAction].
     */
    fun runAction(action: RobotAction)

    /**
     * Runs the provided [block] as a [RobotAction].
     */
    fun runAction(block: RobotActionBlock)
}

class RobotImpl(private val linearOpMode: LinearOpMode) : Robot, CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext get() = Dispatchers.Default + job

    override val hardwareMap: HardwareMap
        get() = linearOpMode.hardwareMap

    private val features = RobotFeatureSet()

    override fun <TConfiguration : RobotFeatureConfiguration, TFeature : RobotFeature> install(
        featureInstaller: RobotFeatureInstaller<TConfiguration, TFeature>,
        configuration: TConfiguration.() -> Unit
    ) {
        val feature = featureInstaller.install(this, configuration)
        features.add(feature)
    }

    override fun <TFeature : RobotFeature> feature(featureClass: KClass<TFeature>): TFeature {
        return features[featureClass]
                ?: throw MissingRobotFeatureException(featureClass.typeParameters.first().toString())
    }

    private fun setup() {
        job = Job()
    }

    override fun setupAndWaitForStart() {
        setup()
        linearOpMode.waitForStart()
    }

    override fun runAction(action: RobotAction): Unit = runBlocking {
        action.run(this@RobotImpl, this@RobotImpl)
    }

    override fun runAction(block: RobotActionBlock): Unit = runBlocking {
        val action = RobotAction(block)
        action.run(this@RobotImpl, this@RobotImpl)
    }
}

/**
 * Reports a situation where a requested [RobotFeature] isn't available.
 */
class MissingRobotFeatureException(override val message: String? = "") : RuntimeException()

/**
 * Creates an instance of [Robot], installing the features requested within the [init] block.
 */
fun createRobot(
    linearOpMode: LinearOpMode,
    shouldSetupAndWaitForStart: Boolean = true,
    init: Robot.() -> Unit
): Robot {
    val robot = RobotImpl(linearOpMode)
    robot.init()
    if (shouldSetupAndWaitForStart) {
        robot.setupAndWaitForStart()
    }
    return robot
}
