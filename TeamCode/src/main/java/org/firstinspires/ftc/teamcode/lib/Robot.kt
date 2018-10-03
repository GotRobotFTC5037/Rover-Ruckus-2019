package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.runBlocking
import kotlin.coroutines.experimental.CoroutineContext

abstract class Robot(val linearOpMode: LinearOpMode) : CoroutineScope {

    abstract val driveTrain: RobotDriveTrain

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined + job

    fun setup() {
        job = Job()
        driveTrain.setup()
    }

    fun start() {
        driveTrain.start()
    }

    fun run(action: RobotAction) = runBlocking {
        action.block(this@Robot)
    }

    fun stop() {
        driveTrain.stop()
    }

}
