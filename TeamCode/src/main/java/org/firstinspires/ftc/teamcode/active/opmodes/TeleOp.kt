@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.active.features.CargoDeliverySystem
import org.firstinspires.ftc.teamcode.active.features.Lift
import org.firstinspires.ftc.teamcode.active.features.MarkerDeployer
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.util.clip
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
            val deliverySystem = requestFeature(CargoDeliverySystem)

            var reversed = false

            // Drive Train Direction
            launch {
                while (true) {
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
            }

            // Drive Train
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

            // Lift
            launch {
                while(true) {
                    lift.setPower(-gamepad2.left_stick_y.toDouble())
                    yield()
                }
            }

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

            launch {
                while (true) {
                    val power = when {
                        gamepad2.left_trigger > 0.5 -> 1.0
                        gamepad2.right_trigger > 0.5 -> -0.5
                        else -> 0.0
                    }
                    deliverySystem.intake.setLiftPower(power)
                    yield()
                }
            }

            launch {
                while (true) {
                    val power = when {
                        gamepad2.b -> 0.75
                        gamepad2.a -> -0.55
                        else -> 0.0
                    }
                    deliverySystem.intake.setIntakePower(power)
                    yield()
                }
            }

            // Popper
            launch {
                while (true) {
                    when (gamepad2.x) {
                        true -> deliverySystem.popper.enablePopper()
                        false -> deliverySystem.popper.disablePopper()
                    }
                    yield()
                }
            }

            // Chute
            launch {
                while (true) {
                    deliverySystem.chute.setChuteLiftPower(
                        -gamepad2.right_stick_y.toDouble()
                    )
                    yield()
                }
            }

            launch {
                while (true) {
                    when {
                        gamepad2.dpad_up -> deliverySystem.chute.raiseShutter()
                        gamepad2.dpad_down -> deliverySystem.chute.dropShutter()
                    }
                    yield()
                }
            }


            // Telemetry
            launch {
                while (true) {
                    telemetry.addData("Reversed?", reversed)
                    telemetry.addData("Position", lift.liftPosition)
                    telemetry.update()
                }
                yield()
            }

            delayUntilStop()
            coroutineContext.cancelChildren()
        }
    }
}


