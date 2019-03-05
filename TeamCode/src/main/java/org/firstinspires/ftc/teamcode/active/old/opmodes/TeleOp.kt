//@file:Suppress("unused")
//
//package org.firstinspires.ftc.teamcode.active.old.opmodes
//
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp
//import com.qualcomm.robotcore.hardware.Gamepad
//import kotlinx.coroutines.*
//import org.firstinspires.ftc.teamcode.active.old.features.CargoDeliverySystem
//import org.firstinspires.ftc.teamcode.active.old.features.Lift
//import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
//import org.firstinspires.ftc.teamcode.lib.robot.perform
//
//
//@TeleOp
//class TeleOp : LinearOpMode() {
//
//    private val driver: Gamepad by lazy { gamepad1 }
//    private val gunner: Gamepad by lazy { gamepad2 }
//
//    @Throws(InterruptedException::class)
//    override fun runOpMode() = runBlocking {
//        roverRuckusRobot(this@TeleOp, this).perform {
//            val driveTrain = getFeature(TankDriveTrain)
//            val lift = getFeature(Lift)
//            val cargoDeliverySystem = getFeature(CargoDeliverySystem)
//            while (isActive) {
//
//                // Drive Train
//                driveTrain.setMotorPowers(
//                    TankDriveTrain.MotorPowers(
//                        right = -gamepad1.left_stick_y.toDouble(),
//                        left = -gamepad1.right_stick_y.toDouble()
//                    )
//                )
//
//                // robot Lift
//                lift.setPower(-gunner.left_stick_y.toDouble())
//
//                // Intake Lift
//                when {
//                    gunner.left_trigger > 0.5 -> cargoDeliverySystem.intake.setLiftPower(1.0)
//                    gunner.right_trigger > 0.5 -> cargoDeliverySystem.intake.setLiftPower(-1.0)
//                    else -> cargoDeliverySystem.intake.setLiftPower(0.0)
//                }
//
//                // Intake
//                when {
//                    gunner.a -> cargoDeliverySystem.intake.setIntakePower(-1.0)
//                    gunner.b -> cargoDeliverySystem.intake.setIntakePower(1.0)
//                    else -> cargoDeliverySystem.intake.setIntakePower(0.0)
//                }
//
//                // Chute
//                cargoDeliverySystem.chute.setChuteLiftPower(gunner.right_stick_y.toDouble())
//
//                // Shutter
//                when {
//                    gunner.dpad_up -> cargoDeliverySystem.chute.raiseShutter()
//                    gunner.dpad_down -> cargoDeliverySystem.chute.dropShutter()
//                }
//
//                // Popper
//                when {
//                    gunner.x -> cargoDeliverySystem.popper.enablePopper()
//                    else -> cargoDeliverySystem.popper.disablePopper()
//                }
//            }
//        }
//    }
//}
//
//
