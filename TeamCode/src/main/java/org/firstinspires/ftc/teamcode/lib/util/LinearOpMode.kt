package org.firstinspires.ftc.teamcode.lib.robot

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.yield

fun LinearOpMode.isAutonomous() = this::class.annotations.any { it is Autonomous }

fun LinearOpMode.isTeleOp() = this::class.annotations.any { it is TeleOp }

suspend fun LinearOpMode.delayUntilStart() {
    while (!isStarted) {
        yield()
    }
}