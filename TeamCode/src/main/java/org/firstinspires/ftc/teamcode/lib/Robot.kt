package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.experimental.*
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class Robot(private val linearOpMode: LinearOpMode) : CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext get() = Dispatchers.Default + job

    private val components = mutableListOf<Component>()

    var driveTrain: RobotDriveTrain
        get() = components.first { it is RobotDriveTrain } as RobotDriveTrain
        set(value) {
            if (components.none { it is RobotDriveTrain }) {
                components.add(value)
            }
        }

    lateinit var localizer: RobotLocalizer
    private set

    fun <T : RobotDriveTrain>driveTrain(driveTrainType: KClass<out T>, init: T.() -> Unit) {
        val driveTrain = driveTrainType.createInstance()
        driveTrain.init()
        components.add(driveTrain)
    }

    fun <T : RobotLocalizer>setLocalizer(localizerType: KClass<out T>) {
        localizer = localizerType.createInstance()
    }

    fun setupAndWaitForStart() {
        setup()
        linearOpMode.waitForStart()
        start()
    }

    private fun setup() {
        job = Job()
        for (component in components) {
            component.setup(linearOpMode)
        }
        localizer.setup(linearOpMode, this)
    }

    private fun start() {
        GlobalScope.launch {
            while (!linearOpMode.isStopRequested) {
                yield()
            }
            job.cancel()
        }
        for (component in components) {
            component.start()
        }
    }

    fun runAction(action: RobotAction) = runBlocking {
        action.run(this@Robot)
    }

    fun stop() {
        for (component in components) {
            component.stop()
        }
        job.cancelChildren()
    }

}

fun createRobot(linearOpMode: LinearOpMode, init: Robot.() -> Unit): Robot {
    val robot = Robot(linearOpMode)
    robot.init()
    return robot
}

