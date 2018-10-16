package org.firstinspires.ftc.teamcode.lib.feature.localizer

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeature

/**
 * Describes the current position of the robot.
 */
data class RobotPosition(
    val linearPosition: Double,
    val lateralPosition: Double
)

/**
 * Reports the current heading of the robot.
 */
interface RobotHeadingLocalizer : RobotFeature {

    val isReady: Boolean

    /**
     * Returns a [ReceiveChannel] that sends the current heading of the robot.
     */
    fun newHeadingChannel(): ReceiveChannel<Double>
}

/**
 * Reports the position of the robot.
 */
interface RobotPositionLocalizer : RobotFeature {

    val isReady: Boolean

    /**
     * Returns a [ReceiveChannel] that sends the current position of the robot.
     */
    fun newPositionChannel(): ReceiveChannel<RobotPosition>
}

