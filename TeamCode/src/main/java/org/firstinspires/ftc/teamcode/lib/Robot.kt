package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.experimental.*
import kotlin.coroutines.experimental.CoroutineContext

abstract class Robot(private val linearOpMode: LinearOpMode) : CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    abstract val driveTrain: RobotDriveTrain

    fun setupAndWaitForStart() {
        setup()
        linearOpMode.waitForStart()
        start()
    }

    fun setup() {
        job = Job()
        driveTrain.setup()
    }

    fun start() {
        driveTrain.start()
        GlobalScope.launch {
            while (!linearOpMode.isStopRequested) {
                yield()
            }
            job.cancel()
        }
    }

    fun run(action: RobotAction) = runBlocking {
        action.run(this@Robot)
    }

    fun stop() {
        driveTrain.stop()
        job.cancelChildren()
    }

}
