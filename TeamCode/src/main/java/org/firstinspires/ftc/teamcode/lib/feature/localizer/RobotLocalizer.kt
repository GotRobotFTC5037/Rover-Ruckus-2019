package org.firstinspires.ftc.teamcode.lib.feature.localizer

import kotlinx.coroutines.channels.BroadcastChannel
import org.firstinspires.ftc.teamcode.lib.feature.Feature

/**
 * Describes the current position of the robot.
 */
data class Position(
    val linearPosition: Double,
    val lateralPosition: Double
)

/**
 * A [Feature] that provides an the robot with information relating but not limited to to the
 * current position, heading, course, speed or acceleration of the robot.
 */
interface Localizer : Feature {

    val isReady: Boolean
}

/**
 * Reports the current heading of the robot.
 */
interface HeadingLocalizer : Localizer {

    val heading: BroadcastChannel<Double>
}

/**
 * Reports the position of the robot.
 */
interface PositionLocalizer : Localizer {

    val position: BroadcastChannel<Position>
}

