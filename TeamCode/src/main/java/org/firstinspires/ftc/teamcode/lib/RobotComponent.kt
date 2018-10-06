package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode

typealias Component = RobotComponent

interface RobotComponent {
    fun start()
    fun setup(linearOpMode: LinearOpMode)
    fun stop()
}