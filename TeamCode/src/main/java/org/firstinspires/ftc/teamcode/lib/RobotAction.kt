package org.firstinspires.ftc.teamcode.lib

abstract class RobotAction {
    abstract suspend fun run(robot: Robot)
}

class RobotMoveAction
private constructor(private val block: suspend Robot.() -> Unit) : RobotAction() {

    var timeout = 0

    override suspend fun run(robot: Robot) {
        block(robot)
    }

    companion object {

        fun linearDistanceDrive(power: Double, distance: Double) = RobotMoveAction {
            driveTrain?.setLinearDrivePower(
                if (distance < 0) Math.abs(power)
                else -Math.abs(power)
            )

            
        }

    }
}
