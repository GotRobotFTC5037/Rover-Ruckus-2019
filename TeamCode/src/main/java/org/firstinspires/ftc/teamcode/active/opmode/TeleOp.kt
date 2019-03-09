package org.firstinspires.ftc.teamcode.active.opmode

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.active.robot.Coda
import org.firstinspires.ftc.teamcode.lib.action.perform
import org.firstinspires.ftc.teamcode.lib.action.turnTo
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.MecanumDriveTrain
import org.firstinspires.ftc.teamcode.lib.opmode.CoroutineOpMode
import org.firstinspires.ftc.teamcode.lib.opmode.driver
import org.firstinspires.ftc.teamcode.lib.robot.Robot

@Suppress("unused", "SpellCheckingInspection")
@TeleOp
class TeleOp : CoroutineOpMode() {

    lateinit var robot: Robot

    override suspend fun initialize() {
        robot = Coda()
    }

    override suspend fun run() = robot.perform {
        val driveTrain = getFeature(MecanumDriveTrain)

        while (isActive) {
            val linearPower = -gamepad1.left_stick_y.toDouble()
            val lateralPower = gamepad1.left_stick_x.toDouble()
            val rotationalPower = gamepad1.right_stick_x.toDouble()
            driveTrain.setDirectionPower(linearPower, lateralPower, rotationalPower)

            when {
                driver.dpad_up -> robot.perform(turnTo(0.0))
                driver.dpad_left -> robot.perform(turnTo(90.0))
                driver.dpad_right -> robot.perform(turnTo(-90.0))
                driver.dpad_down -> robot.perform(turnTo(180.0))
            }

            yield()
        }
    }

}