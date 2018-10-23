package org.firstinspires.ftc.teamcode.active.test

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.isActive
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.robot.robot

@TeleOp(name = "Test: IMULocalizer", group = "Tests")
class IMULocalizerTest : LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        robot(this) {
            install(IMULocalizer) {
                pollRate = 250 // 250 is the update rate of [Telemetry].
            }
        }.perform {
            val imuLocalizer = requestFeature(IMULocalizer)
            val headingChannel = imuLocalizer.heading.openSubscription()
            val line = telemetry.addLine()
            while (isActive) {
                line.addData("Heading", headingChannel.receive())
                telemetry.update()
            }
            headingChannel.cancel()
        }
    }

}