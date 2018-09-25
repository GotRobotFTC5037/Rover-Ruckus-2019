package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch

/**
 * The [Robot] class is the fundamental core of the library. Instances of this class are what run
 * instances of [RobotAction] and manage their usage.
 */
abstract class Robot {
    internal abstract val opMode: LinearOpMode
    internal abstract val driveTrain: RobotDriveTrain?

    private val actionJobs = mutableListOf<RobotActionJob>()
    val hasAction get() = actionJobs.isNotEmpty()

    fun setup() {
        driveTrain?.setup()
    }

    fun start() {
        // TODO: Do something here.
    }

    fun setupAndWaitForStart() {
        setup()
        opMode.waitForStart()
        start()
    }

    fun runAction(action: RobotAction): RobotActionJob =
        GlobalScope.launch(start = CoroutineStart.LAZY) { action.run(this@Robot) }
            .also {
                actionJobs.add(it)
            }.apply {
                invokeOnCompletion { actionJobs.remove(this) }
                start()
            }

    fun waitForActionsToComplete() {
        while (hasAction) {
            opMode.idle()
        }
    }

    companion object

}

