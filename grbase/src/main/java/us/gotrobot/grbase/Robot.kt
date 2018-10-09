package us.gotrobot.grbase

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.runBlocking
import kotlin.coroutines.experimental.CoroutineContext

class MissingRobotFeatureException(override val message: String?) : Exception()

interface Robot : CoroutineScope {

    val hardwareMap: HardwareMap

    fun <C : RobotFeatureConfiguration, F : Any>install(
        feature: RobotFeature<C, F>,
        configure: C.() -> Unit
    )

    fun <T : Any>feature(feature: RobotFeatureDescriptor<T>): T

    fun runAction(action: Action)
}

internal class RobotImpl(private val linearOpMode: LinearOpMode) : Robot {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext get() = Dispatchers.Default + job

    override val hardwareMap: HardwareMap
        get() = linearOpMode.hardwareMap

    private val features = mutableMapOf<RobotFeatureKey<*>, Any>()

    override fun <C : RobotFeatureConfiguration, F : Any> install(
        feature: RobotFeature<C, F>,
        configure: C.() -> Unit
    ) {
        if (features[feature.key] == null) {
            features[feature.key] = feature.install(this, configure)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> feature(feature: RobotFeatureDescriptor<T>): T {
        return features[feature.key] as T?
                ?: throw MissingRobotFeatureException("Robot does not the have the feature '${feature.key.name}' installed.")
    }

    override fun runAction(action: Action) = runBlocking {
        action.run(this@RobotImpl, this@RobotImpl)
    }

}

fun createRobot(linearOpMode: LinearOpMode, init: Robot.() -> Unit): Robot {
    val robot = RobotImpl(linearOpMode)
    robot.init()
    return robot
}