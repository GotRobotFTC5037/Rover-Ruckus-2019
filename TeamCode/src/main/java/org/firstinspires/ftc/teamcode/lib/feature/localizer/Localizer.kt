package org.firstinspires.ftc.teamcode.lib.feature.localizer

import org.firstinspires.ftc.teamcode.lib.feature.Feature

/**
 * A [Feature] that provides an the robot with information relating, but not limited to, current
 * position, heading, course, speed or acceleration of the robot.
 */
interface RobotLocalizer : Feature

interface RobotHeadingLocalizer: RobotLocalizer

interface RobotPositionLocalizer: RobotLocalizer
