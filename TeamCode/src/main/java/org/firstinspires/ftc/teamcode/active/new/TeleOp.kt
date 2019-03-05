package org.firstinspires.ftc.teamcode.active.new

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.action.perform
import org.firstinspires.ftc.teamcode.lib.opmode.CoroutineOpMode
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.MecanumDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.robot
import org.firstinspires.ftc.teamcode.lib.robot.install

@Suppress("unused", "SpellCheckingInspection")
@TeleOp
class TeleOp : CoroutineOpMode() {

    lateinit var robot: Robot

    override suspend fun initialize() {
        robot = robot {
            install(MecanumDriveTrain) {
                frontLeftMotorName = "front left motor"
                frontRightMotorName = "front right motor"
                backLeftMotorName = "back left motor"
                backRightMotorName = "back right motor"
            }
            install(IMULocalizer) {
                imuName = "imu"
            }
        }
    }

    override suspend fun run() {
        robot.perform {
            val driveTrain = getFeature(MecanumDriveTrain)
            while (isActive) {
                val linearPower = -gamepad1.left_stick_y.toDouble()
                val lateralPower = gamepad1.left_stick_x.toDouble()
                val rotationalPower = gamepad1.right_stick_x.toDouble()
                driveTrain.setDirectionPower(linearPower, lateralPower, rotationalPower)
                yield()
            }
        }
    }

}