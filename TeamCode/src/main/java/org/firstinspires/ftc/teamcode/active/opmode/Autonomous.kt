package org.firstinspires.ftc.teamcode.active.opmode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import org.firstinspires.ftc.teamcode.active.robot.Coda
import org.firstinspires.ftc.teamcode.lib.action.followTrajectory
import org.firstinspires.ftc.teamcode.lib.opmode.CoroutineOpMode
import org.firstinspires.ftc.teamcode.lib.robot.Robot

@Suppress("unused", "SpellCheckingInspection")
@Autonomous
class DepotAutonomous : CoroutineOpMode() {

    lateinit var robot: Robot

    override suspend fun initialize() {
        robot = Coda()
    }

    override suspend fun run() {

    }

}

@Suppress("unused", "SpellCheckingInspection")
@Autonomous
class CraterAutonomous : CoroutineOpMode() {

    lateinit var robot: Robot

    private val trajectory = Coda.buildTrajectory {
        forward(50.0)
        forward(-25.0)
        strafeLeft(100.0)
        turn(-45.0)
        forward(-100.0)
    }

    override suspend fun initialize() {
        robot = Coda()
    }

    override suspend fun run() {
        robot.perform(followTrajectory(trajectory))
    }
}