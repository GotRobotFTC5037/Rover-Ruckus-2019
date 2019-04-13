package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.isActive
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

        while (isActive) {
            val linearPower = -gamepad1.left_stick_y.toDouble()
            val lateralPower = gamepad1.left_stick_x.toDouble()
            val rotationalPower = gamepad1.right_stick_x.toDouble()
            driveTrain.setDirectionPower(linearPower, lateralPower, rotationalPower)

            when {
                gamepad1.dpad_up -> lift.setLiftMotorPower(1.0)
                gamepad1.dpad_down -> if (lift.isLowered.not()) lift.setLiftMotorPower(-1.0)
                else -> lift.setLiftMotorPower(0.0)
            }

            when {
                gamepad2.dpad_up -> cargoDelivery.setRotationalMotorPosition(2100)
                gamepad2.dpad_down -> cargoDelivery.setRotationalMotorPosition(0)
            }

            when {
                gamepad1.y -> {
                    markerDeployer.toggle()
                    while (gamepad1.y) {
                        yield()
                    }
                }
            }

            when {
                gamepad1.left_trigger >= 0.5 -> {
                    cargoDelivery.setSortingDirection(CargoDeliverySystem.SortingDirection.LEFT)
                }
                gamepad1.right_trigger >= 0.5 -> {
                    cargoDelivery.setSortingDirection(CargoDeliverySystem.SortingDirection.RIGHT)
                }
            }

            cargoDelivery.setRotationMotorPower(-gamepad2.left_stick_y.toDouble())
            cargoDelivery.setExtensionMotorPower(-gamepad2.right_stick_y.toDouble())

            when {
                gamepad2.a -> cargoDelivery.setIntakeStatus(CargoDeliverySystem.IntakeStatus.ADMIT)
                gamepad2.b -> cargoDelivery.setIntakeStatus(CargoDeliverySystem.IntakeStatus.EJECT)
                else -> cargoDelivery.setIntakeStatus(CargoDeliverySystem.IntakeStatus.STOPPED)
            }

            yield()
        }
    }

}
