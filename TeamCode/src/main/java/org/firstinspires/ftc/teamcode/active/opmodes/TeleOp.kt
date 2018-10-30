package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.lib.action.performTeleOp
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.robot.robot

@TeleOp
class TeleOp : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        robot(this) {
            install(TankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
        }.performTeleOp {
            val driveTrain = requestFeature(TankDriveTrain)
            controlScheme {
                gamepad1.joysticks {
                    driveTrain.setMotorPowers(leftY, rightY)
                }
            }
        }
    }

}
