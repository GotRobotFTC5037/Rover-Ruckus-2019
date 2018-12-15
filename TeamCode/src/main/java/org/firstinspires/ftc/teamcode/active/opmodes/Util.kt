@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.teamcode.active.Lift
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.robot.robot
import org.firstinspires.ftc.teamcode.lib.util.Utility

@Autonomous
class RetractLift : LinearOpMode(), Utility {
    override fun runOpMode() = runBlocking {
        robot(this@RetractLift, this) {
            install(Lift) {
                liftMotorName = "lift motor"
            }
        }.perform {
            val lift = requestFeature(Lift)
            lift.retract()
        }
    }
}

@TeleOp
class IMUData : LinearOpMode() {
    override fun runOpMode() = runBlocking {
        robot(this@IMUData, this) {
            install(IMULocalizer) {
                imuName = "imu"
                order = AxesOrder.XYZ
            }
        }.perform {
            val imu = requestFeature(IMULocalizer)
            val orientation = imu.newOrientationChannel()
            while (true) {
                val update = orientation.receive()
                telemetry.addLine()
                    .addData("X", update.heading)
                    .addData("Y", update.pitch)
                    .addData("Z", update.roll)
                telemetry.update()
                yield()
            }
        }
    }
}