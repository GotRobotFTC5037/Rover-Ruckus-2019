package us.gotrobot.grbase

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.experimental.*
import kotlin.coroutines.experimental.CoroutineContext

class MissingRobotComponentExeption(override val message: String?): Exception()

interface Robot : Component {

    val hardwareMap: HardwareMap

    fun <C : ComponentConfiguration, T : Component> addComponent(
        installer: RobotComponentInstaller<C, T>,
        configure: C.() -> Unit
    )

    fun <C : Component>getComponent(installer: ComponentInstaller<*, C>): C

    fun runAction(action: Action): Job

    fun joinCurrentAction(): Unit?
}

internal class RobotImpl(private val linearOpMode: LinearOpMode) : Robot, CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext get() = Dispatchers.Default + job

    private val components = mutableMapOf<ComponentInstallerKey<*>, Component>()

    private var currentJob: Job? = null

    override val hardwareMap: HardwareMap
        get() = linearOpMode.hardwareMap

    override fun <C : ComponentConfiguration, T : Component> addComponent(
        installer: RobotComponentInstaller<C, T>,
        configure: C.() -> Unit
    ) {
        val component = installer.install(this, configure)
        components[installer.key] = component
    }

    @Suppress("UNCHECKED_CAST")
    override fun <C : Component> getComponent(installer: ComponentInstaller<*, C>): C =
        components[installer.key] as? C ?: throw MissingRobotComponentExeption(installer.key.name)

    override fun runAction(action: Action): Job {
        if (currentJob?.isActive == true) runBlocking { currentJob?.join() }
        val job = launch {
            action.run(this@RobotImpl, this)
        }
        currentJob = job
        return job
    }

    override fun joinCurrentAction() = runBlocking { currentJob?.run { join() } }

}

fun createRobot(linearOpMode: LinearOpMode, init: Robot.() -> Unit): Robot {
    val robot =  RobotImpl(linearOpMode)
    robot.init()
    return robot
}