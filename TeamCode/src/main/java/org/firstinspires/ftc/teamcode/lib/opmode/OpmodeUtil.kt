package org.firstinspires.ftc.teamcode.lib.opmode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import kotlin.reflect.full.findAnnotation

val OpMode.driver: Gamepad get() = gamepad1
val OpMode.gunner: Gamepad get() = gamepad2

val OpMode.isAutonomous: Boolean get() = this::class.findAnnotation<Autonomous>() != null
val OpMode.isTeleOp: Boolean get() = this::class.findAnnotation<TeleOp>() != null