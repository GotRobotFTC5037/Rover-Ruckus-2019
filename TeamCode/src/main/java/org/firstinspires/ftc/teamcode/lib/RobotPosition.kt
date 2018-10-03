package org.firstinspires.ftc.teamcode.lib

data class RobotVector(
    val linear: Double = 0.0,
    val lateral: Double = 0.0,
    val heading: Double = 0.0
)

data class RobotLocation(
    val xPosition: Double,
    val yPosition: Double,
    val heading: Double,
    val course: Double?
)