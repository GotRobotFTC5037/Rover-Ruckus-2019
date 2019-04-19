package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import us.gotrobot.grbase.action.feature
import us.gotrobot.grbase.action.perform
import us.gotrobot.grbase.feature.HeadingCorrection
import us.gotrobot.grbase.feature.drivetrain.MecanumDriveTrain
import us.gotrobot.grbase.opmode.CoroutineOpMode
import us.gotrobot.grbase.robot.Robot

@Suppress("unused", "SpellCheckingInspection")
@TeleOp(name = "TeleOp")
class TeleOp : CoroutineOpMode() {

    lateinit var robot: Robot

    override suspend fun initialize() {
        robot = Metabot().apply {
            features[HeadingCorrection].enabled = false
        }
    }

    override suspend fun run() = robot.perform {
        val driveTrain = feature(MecanumDriveTrain)
        val cargoDelivery = feature(CargoDeliverySystem)
        val markerDeployer = feature(MarkerDeployer)

        var reversed = false

        var rotationJob: Job? = null

        while (isActive) {
            val multiplyer =
                (if (gamepad1.left_stick_button) 1.0 else 0.65) * (if (reversed) 1 else -1)
            val linearPower = -gamepad1.left_stick_y.toDouble() * multiplyer
            val lateralPower = gamepad1.left_stick_x.toDouble() * multiplyer
            val rotationalPower = gamepad1.right_stick_x.toDouble()
            driveTrain.setDirectionPower(linearPower, lateralPower, rotationalPower)

            when {
                gamepad1.y -> robotLift.setLiftMotorPower(1.0)
                gamepad1.a -> if (robotLift.isLowered.not()) robotLift.setLiftMotorPower(-1.0)
                else -> robotLift.setLiftMotorPower(0.0)
            }

            if (gamepad1.start) {
                reversed = !reversed
                while (gamepad1.start) {
                    yield()
                }
            }

            if (gamepad2.y) {
                markerDeployer.toggle()
                while (gamepad2.y) {
                    yield()
                }
            }

            when {
                gamepad2.dpad_left ->
                    cargoDelivery.setSortingDirection(CargoDeliverySystem.SortingDirection.LEFT)
                gamepad2.dpad_right ->
                    cargoDelivery.setSortingDirection(CargoDeliverySystem.SortingDirection.RIGHT)
            }

            when {
                gamepad2.left_trigger >= 0.5 ->
                    cargoDelivery.setRotationPower(1.0)
                gamepad2.right_trigger >= 0.5 ->
                    cargoDelivery.setRotationPower(-1.0)
                else -> cargoDelivery.setRotationPower(0.0)
            }
            cargoDelivery.setExtensionPower(gamepad2.left_stick_y.toDouble())

            when {
                gamepad2.a -> cargoDelivery.setIntakeStatus(CargoDeliverySystem.IntakeStatus.ADMIT)
                gamepad2.b -> cargoDelivery.setIntakeStatus(CargoDeliverySystem.IntakeStatus.EJECT)
                else -> cargoDelivery.setIntakeStatus(CargoDeliverySystem.IntakeStatus.STOPPED)
            }

            when {
                gamepad2.dpad_up ->  {
                    while (gamepad2.dpad_up) {
                        yield()
                    }
                    if (rotationJob == null || !rotationJob.isActive) {
                        rotationJob = launch {
                            cargoDelivery.setRotationPosition(1500)
                        }
                    }
                }
                gamepad2.dpad_down ->  {
                    while (gamepad2.dpad_down) {
                        yield()
                    }
                    if (rotationJob == null || !rotationJob.isActive) {
                        rotationJob = launch {
                            cargoDelivery.setRotationPosition(0)
                        }
                    }
                }
            }

        }
    }
}




