package org.firstinspires.ftc.teamcode.active.production

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.isActive
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.robot.robot
import kotlin.coroutines.CoroutineContext

@Autonomous
class Autonomous : LinearOpMode() {

    /**
     * The sequence of actions that are performed when the robot detects the gold on the left.
     */
    private val leftAction = actionSequenceOf(
        turnTo(35.0, 0.3) then wait(100),
        driveTo(720, 0.4),
        turnTo(-10.0, 0.3) then wait(100),
        driveTo(1080, 0.4),
        driveTo(-200, 0.4),
        turnTo(-110.0, 0.3) then wait(100),
        driveTo(1800, 0.6)

    )

    /**
     * The sequence of actions that are performed when the robot detects the gold in the center.
     */
    private val centerAction = actionSequenceOf(
        driveTo(1801, 0.4),
        driveTo(-200, 0.4) then wait(100),
        turnTo(-110.0, 0.3) then wait(100)
    )

    /**
     * The sequence of actions that are performed when the robot detects the gold on the right.
     */
    private val rightAction = actionSequenceOf(
        turnTo(-35.0, 0.3) then wait(100),
        driveTo(720, 0.4),
        turnTo(10.0, 0.3) then wait(100),
        driveTo(1080, 0.4),
        driveTo(-200, 0.4),
        turnTo(-110.0, 0.3) then wait(100),
        driveTo(1800, 0.5)
    )

    val main = action {
        // Checkout the potentiometer.
        val potentiometer = requestFeature(Potentiometer)

        // Subscribe to the angle broadcasting channel.
        val angleChannel = potentiometer.angle.openSubscription()

        // Check the potentiometer angle and choose which action to run.
        when (angleChannel.receive()) {
            in 0.0..90.0 -> perform(leftAction)
            in 90.0..180.0 -> perform(centerAction)
            in 180.0..270.0 -> perform(rightAction)
        }

        // Close the angle channel and unsubscribe from the channel.
        angleChannel.cancel()
    }

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        robot(this) {
            install(TankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
            install(IMULocalizer)
            install(GoldDetector)
            install(Potentiometer)
        }.perform {}
        while (opModeIsActive()) {}
    }

}


