@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.active.Intake
import org.firstinspires.ftc.teamcode.active.MarkerDeployer
import org.firstinspires.ftc.teamcode.active.RobotLift
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.feature.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.feature.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.robot.perform


@TeleOp
class TeleOp : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@TeleOp, this).perform {
            val driveTrain = requestFeature(TankDriveTrain)
            val lift = requestFeature(RobotLift)
            val deployer = requestFeature(MarkerDeployer)
            val imuLocalizer = requestFeature(IMULocalizer)
            val tankDriveLocalizer = requestFeature(TankDriveTrain.Localizer)
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
                            -gamepad1.left_stick_y.toDouble(),
                            -gamepad1.right_stick_y.toDouble()
                        )
                    } else {
                        driveTrain.setMotorPowers(
                            gamepad1.right_stick_y.toDouble(),
                            gamepad1.left_stick_y.toDouble()
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
                        gamepad2.right_trigger > 0.5 -> (0.5)
                        gamepad2.left_trigger > 0.5 -> (-0.5)
                        else -> 0.0
                    }
                    intake.setLiftPower(power)
                    yield()
                }
            }
            launch {
                while (true) {
                    if (gamepad2.a) {


                    }
                }
            }

            while (!isStopRequested) {
                yield()
            }

        }
    }
}


