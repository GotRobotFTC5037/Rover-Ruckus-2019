package org.firstinspires.ftc.teamcode.lib

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.withTimeout

typealias RobotActionBlock = suspend Robot.() -> Unit

sealed class RobotAction {
    abstract suspend fun run(robot: Robot)
}

class RobotMoveAction private constructor(val block: RobotActionBlock) : RobotAction() {

    var timoutDuration = Long.MAX_VALUE

    override suspend fun run(robot: Robot) {
        withTimeout(timoutDuration) {
            block(robot)
        }
    }

    companion object {
        fun linearTimeDrive(power: Double, duration: Long) = RobotMoveAction {
            driveTrain.setPower(RobotVector(linear = power))
            delay(duration)
            driveTrain.stop()
        }

        fun timeTurn(power: Double, duration: Long) = RobotMoveAction {
            driveTrain.setPower(RobotVector(heading = power))
            delay(duration)
            driveTrain.stop()
        }
    }

}
