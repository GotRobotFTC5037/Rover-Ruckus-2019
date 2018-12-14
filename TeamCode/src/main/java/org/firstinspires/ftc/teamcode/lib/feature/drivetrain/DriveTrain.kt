package org.firstinspires.ftc.teamcode.lib.feature.drivetrain

import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.firstinspires.ftc.teamcode.lib.feature.Feature

typealias MotorDirection = DcMotorSimple.Direction

interface DriveTrain : Feature {
    fun stop()
}
