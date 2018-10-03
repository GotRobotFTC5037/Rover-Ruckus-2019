package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job

abstract class Robot(val linearOpMode: LinearOpMode) : CoroutineScope {

    abstract val driveTrain: RobotDriveTrain

    lateinit var job: Job

    fun setup() {
        job = Job()
        driveTrain.setup()
    }

    fun start() {
        driveTrain.start()
    }

    fun run(action: RobotAction) {
        action.block(this)
    }

    fun stop() {
        driveTrain.stop()
    }

}
