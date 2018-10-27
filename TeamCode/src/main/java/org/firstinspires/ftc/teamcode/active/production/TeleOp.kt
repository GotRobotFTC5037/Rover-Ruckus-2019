package org.firstinspires.ftc.teamcode.active.production

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.isActive
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.robot.perform
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
        }.perform {
            val driveTrain = requestFeature(TankDriveTrain)
            while (isActive) {
                driveTrain.setMotorPowers(
                    -gamepad1.left_stick_y.toDouble(),
                    -gamepad1.right_stick_y.toDouble()
                )
            }
        }
    }

}
