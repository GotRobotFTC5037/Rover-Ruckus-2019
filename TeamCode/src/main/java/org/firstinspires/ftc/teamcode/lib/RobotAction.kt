package org.firstinspires.ftc.teamcode.lib

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.yield
import kotlin.math.abs

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
        fun linearTimeDrive(duration: Long, power: Double) = RobotMoveAction {
            driveTrain.setPower(RobotVector(linear = power))
            delay(duration)
            driveTrain.stop()
        }

        fun timeTurn(duration: Long, power: Double) = RobotMoveAction {
            driveTrain.setPower(RobotVector(heading = power))
            delay(duration)
            driveTrain.stop()
        }

        fun turnTo(targetHeading: Double, power: Double) = RobotMoveAction {
            val location = localizer.locationProducer
            val currentHeading = location.receive().heading
            val predicate: suspend () -> Boolean
            when {
                targetHeading > currentHeading -> {
                    driveTrain.setPower(RobotVector(heading = abs(power)))
                    predicate = { targetHeading > location.receive().heading }
                }
                targetHeading < currentHeading -> {
                    driveTrain.setPower(RobotVector(heading = -abs(power)))
                    predicate = { targetHeading < location.receive().heading }
                }
                else -> {
                    predicate = { false }
                }
            }
            while (predicate()) {
                yield()
            }
            driveTrain.stop()
        }
    }

}
