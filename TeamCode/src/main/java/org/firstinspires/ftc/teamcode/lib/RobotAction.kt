package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.util.ElapsedTime

typealias RobotActionBlock = Robot.() -> Unit

sealed class RobotAction {
    internal abstract val block: RobotActionBlock
}

class RobotMoveAction private constructor(override val block: RobotActionBlock) : RobotAction() {

    companion object {
        fun linearTimeDrive(power: Double, duration: Long) = RobotMoveAction {
            val elapsedTime = ElapsedTime()
            driveTrain.setPower(RobotVector(linear = power))
            while (elapsedTime.milliseconds() < duration) {
                linearOpMode.idle()
            }
            driveTrain.stop()
        }

        fun timeTurn(power: Double, duration: Long) = RobotMoveAction {
            val elapsedTime = ElapsedTime()
            driveTrain.setPower(RobotVector(heading = power))
            while (elapsedTime.milliseconds() < duration) {
                linearOpMode.idle()
            }
            driveTrain.stop()
        }
    }

}
