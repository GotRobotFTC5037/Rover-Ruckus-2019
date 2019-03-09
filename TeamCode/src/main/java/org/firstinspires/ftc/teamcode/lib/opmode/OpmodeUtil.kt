package org.firstinspires.ftc.teamcode.lib.opmode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.Gamepad

val OpMode.driver: Gamepad get() = gamepad1
val OpMode.gunner: Gamepad get() = gamepad2