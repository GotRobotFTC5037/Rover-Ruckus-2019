package org.firstinspires.ftc.teamcode.lib.feature.localizer

import kotlinx.coroutines.experimental.channels.BroadcastChannel
import org.firstinspires.ftc.teamcode.lib.feature.Feature

/**
 * Describes the current position of the robot.
 */
data class Position(
    val linearPosition: Double,
    val lateralPosition: Double
)

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

