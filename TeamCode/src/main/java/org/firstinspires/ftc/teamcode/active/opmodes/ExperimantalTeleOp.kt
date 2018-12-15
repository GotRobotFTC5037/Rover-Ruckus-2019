package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.active.Intake
import org.firstinspires.ftc.teamcode.active.MarkerDeployer
import org.firstinspires.ftc.teamcode.active.Lift
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.action.action
import org.firstinspires.ftc.teamcode.lib.driverControl
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain

class ExperimantalTeleOp : LinearOpMode() {

    private val teleOp = action {
        val driveTrain = requestFeature(TankDriveTrain)
        val lift = requestFeature(Lift)
        val deployer = requestFeature(MarkerDeployer)
        val intake = requestFeature(Intake)

        driverControl {
            var driveTrainReversed = false

            loop {
                while (gamepad1.start) {
                    yield()
                }
                driveTrainReversed = !driveTrainReversed
                while (!gamepad1.start) {
                    yield()
                }
            }

            loop {
                if (!driveTrainReversed) {
                    driveTrain.setMotorPowers(
                        -gamepad1.left_stick_y.toDouble(),
                        -gamepad1.right_stick_y.toDouble()
                    )
                } else {
                    driveTrain.setMotorPowers(
                        gamepad1.right_stick_y.toDouble(),
                        gamepad1.left_stick_y.toDouble()
                    )
                }
            }

            loop {
                lift.setPower(-gamepad2.left_stick_y.toDouble())
            }

            var isDeployerExtended = false
            loop {
                if (gamepad2.y) {
                    if (isDeployerExtended) {
                        deployer.deploy()
                    } else {
                        deployer.retract()
                    }
                }
                while (gamepad2.y) {
                    yield()
                }
                isDeployerExtended = !isDeployerExtended
            }

            loop {
                val power = when {
                    gamepad2.right_trigger > 0.5 -> (0.5)
                    gamepad2.left_trigger > 0.5 -> (-0.5)
                    else -> 0.0
                }
                intake.setLiftPower(power)
            }
        }
    }

    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@ExperimantalTeleOp, this).perform(teleOp)
    }
}