package org.firstinspires.ftc.teamcode.active.opmode

import org.firstinspires.ftc.teamcode.active.robot.Coda
import org.firstinspires.ftc.teamcode.lib.opmode.CoroutineOpMode
import org.firstinspires.ftc.teamcode.lib.robot.Robot

class DepotAutonomous : CoroutineOpMode() {

    lateinit var robot: Robot

    override suspend fun initialize() {
        robot = Coda()
    }

    override suspend fun run() {

    }
}