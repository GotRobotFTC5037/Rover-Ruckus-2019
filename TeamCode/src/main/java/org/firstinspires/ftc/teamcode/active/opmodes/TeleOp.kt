@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.active.features.Intake
import org.firstinspires.ftc.teamcode.active.features.Lift
import org.firstinspires.ftc.teamcode.active.features.MarkerDeployer
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.util.delayUntilStop


@TeleOp
class TeleOp : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@TeleOp, this).perform {
            val driveTrain = requestFeature(TankDriveTrain)
            val lift = requestFeature(Lift)
            val deployer = requestFeature(MarkerDeployer)
            val intake = requestFeature(Intake)

            var reversed = false

            launch {
                while (true) {
                    while (gamepad1.start) {
                        yield()
                    }
                    reversed = !reversed
                    while (!gamepad1.start) {
                        yield()
                    }
                }
            }

            launch {
                while (true) {
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
            }

            launch {
                while (true) {
                    lift.setPower(-gamepad2.left_stick_y.toDouble())
                    yield()
                }
            }

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
                }
            }

            launch {
                while (true) {
                    val power = when {
                        gamepad2.right_trigger > 0.5 -> 1.0
                        gamepad2.left_trigger > 0.5 -> -0.5
                        else -> 0.0
                    }
                    intake.setLiftPower(power)
                    yield()
                }
            }

            launch {
                while (true) {
                    val power = when {
                        gamepad2.a -> 0.60
                        gamepad2.b -> -0.40
                        else -> 0.0
                    }
                    intake.setIntakePower(power)
                    yield()
                }
            }

            delayUntilStop()
            coroutineContext.cancelChildren()
        }
    }
}


