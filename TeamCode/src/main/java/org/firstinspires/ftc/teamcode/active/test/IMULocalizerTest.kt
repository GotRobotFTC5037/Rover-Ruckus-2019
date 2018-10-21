package org.firstinspires.ftc.teamcode.active.test

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.experimental.isActive
import org.firstinspires.ftc.teamcode.lib.action.perform
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.createRobot

@TeleOp(name = "Test: IMULocalizer", group = "Tests")
class IMULocalizerTest : LinearOpMode() {

    private val mainAction = perform {
        val imuLocalizer = requestFeature(IMULocalizer)
        val headingChannel = imuLocalizer.heading.openSubscription()
        val line = telemetry.addLine()
        while (isActive) {
            line.addData("Heading", headingChannel.receive())
            telemetry.update()
        }
        headingChannel.cancel()
    }

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        val robot = createRobot(this) {
            install(IMULocalizer) {
                pollRate = 250
            }
        }
        robot.perform(mainAction)
    }

}