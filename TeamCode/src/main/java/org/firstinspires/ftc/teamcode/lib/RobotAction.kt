package org.firstinspires.ftc.teamcode.lib

import kotlinx.coroutines.experimental.delay

typealias RobotActionBlock = suspend Robot.() -> Unit
typealias MoveAction = RobotMoveAction

sealed class RobotAction {
    abstract suspend fun run(robot: Robot)
}

class RobotMoveAction private constructor(val block: RobotActionBlock) : RobotAction() {

    override suspend fun run(robot: Robot) {
        block(robot)
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
