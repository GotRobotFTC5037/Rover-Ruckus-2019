package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.experimental.*

abstract class Robot {
    internal abstract val linearOpMode: LinearOpMode
    internal abstract val driveTrain: RobotDriveTrain
    internal abstract val localizer: RobotLocalizer

    fun<R> runAction(action: RobotAction<R>): Deferred<R> = GlobalScope.async {
        action.block(this@Robot)
    }

    fun runAction(action: RobotAction<Unit>): Job = GlobalScope.launch {
        action.block(this@Robot)
    }
}
