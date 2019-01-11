package org.firstinspires.ftc.teamcode.lib.action

tailrec fun properHeading(heading: Double): Double = when {
    heading >= 180.0 -> properHeading(heading - 360)
    heading < -180.0 -> properHeading(heading + 360)
    else -> heading
}