@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.active.RobotConstants
import org.firstinspires.ftc.teamcode.active.features.Intake
import org.firstinspires.ftc.teamcode.active.features.Lift
import org.firstinspires.ftc.teamcode.active.features.MarkerDeployer
import org.firstinspires.ftc.teamcode.active.features.Popper
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.util.delayUntilStop
import org.firstinspires.ftc.teamcode.lib.util.loop


@TeleOp
class TeleOp : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@TeleOp, this).perform {
            val driveTrain = requestFeature(TankDriveTrain)
            val lift = requestFeature(Lift)
            val deployer = requestFeature(MarkerDeployer)
            val popper = requestFeature(Popper)
            val intake = requestFeature(Intake)

            var reversed = false

            // Drive Train Direction
            loop {
                while (gamepad1.start) {
                    yield()
                }
                reversed = !reversed
                yield()
                while (!gamepad1.start) {
                    yield()
                }
                yield()
            }

            // Drive Train
            loop {
                if (!reversed) {
                    driveTrain.setMotorPowers(
                            -gamepad1.right_stick_y.toDouble(),
                            -gamepad1.left_stick_y.toDouble()
                    )
                } else {
                    driveTrain.setMotorPowers(
                            gamepad1.left_stick_y.toDouble(),
                            gamepad1.right_stick_y.toDouble()
                    )
                }
                yield()
            }

            // Lift
            loop { lift.setPower(-gamepad2.left_stick_y.toDouble()) }

            // Marker Deployer
            launch {
                var isDeployerExtended = false
                while (true) {
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
                    yield()
                }
            }

            // Cargo Intake Lift
            loop {
                val power = when {
                    gamepad2.left_trigger > 0.5 -> 1.0
                    gamepad2.right_trigger > 0.5 -> -0.5
                    else -> 0.0
                }
                intake.setLiftPower(power)
            }

            // Cargo Intake
            loop {
                val power = when {
                    gamepad2.b -> 0.75
                    gamepad2.a -> -0.55
                    else -> 0.0
                }
                intake.setIntakePower(power)
            }

            // Popper
            loop {
                when (gamepad2.x) {
                    true -> popper.activate()
                    false -> popper.deactivate()
                }
            }

            // Telemetry
            loop {
                telemetry.addData("Reversed?", reversed)
                telemetry.addData("Position", lift.liftPosition)
                telemetry.update()
            }

            delayUntilStop()
            coroutineContext.cancelChildren()
        }
    }
}


