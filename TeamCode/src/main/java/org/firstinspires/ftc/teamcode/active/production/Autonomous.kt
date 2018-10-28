@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active.production

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.robot.robot

@Autonomous
class Autonomous : LinearOpMode() {

    /**
     * The sequence of actions that are performed when the robot detects the gold on the left.
     */
    private val leftAction = actionSequenceOf(
        turnTo(19.0, 1.0) then wait(100),
        driveTo(1050, 0.7),
        turnTo(-19.0, 1.0) then wait(100),
        driveTo(720, 0.7) then wait(3000),
        driveTo(-130, 0.7),
        turnTo(-80.0, 1.0) then wait(100),
        driveTo(710, 0.7),
        turnTo(-120.0, 1.0) then wait(100),
        driveTo(360, 0.7)
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
        turnTo(-19.0, 0.3) then wait(100),
        driveTo(1050, 0.4),
        turnTo(19.0, 0.3) then wait(100),
        driveTo(720, 0.4) then wait(3000),
        driveTo(-130, 0.4),
        turnTo(-130.0, 0.4) then wait(100),
        driveTo(1800, 0.5)
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        robot(this) {
            install(TankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
            install(Potentiometer)
            install(TankDriveTrain.Localizer)
            install(GoldDetector)
        }.perform {
            // Checkout the potentiometer.
            val positionDetector = requestFeature(Potentiometer)

            // Subscribe to the angle broadcasting channel.
            val positionChannel = positionDetector.angle.openSubscription()
            val currentPosition = positionChannel.receive()
            positionChannel.cancel()

            // Check the potentiometer angle and choose which action to run.
            when (currentPosition) {
                in 0.0..90.0 -> perform(leftAction)
                in 90.0..180.0 -> perform(centerAction)
                in 180.0..270.0 -> perform(rightAction)
            }
        }
    }

}


