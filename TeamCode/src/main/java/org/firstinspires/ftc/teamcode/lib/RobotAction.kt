package org.firstinspires.ftc.teamcode.lib

class RobotAction<R> private constructor(
    internal val block: suspend Robot.() -> R
) {

    companion object {
        fun <R> customAction(block: suspend Robot.() -> R) = RobotAction(block)
    }

}
