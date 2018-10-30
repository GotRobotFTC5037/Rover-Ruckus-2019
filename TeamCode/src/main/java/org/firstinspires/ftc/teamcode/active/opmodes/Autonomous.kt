@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.channels.consumeEach
import org.firstinspires.ftc.teamcode.active.features.CargoPositionDetector
import org.firstinspires.ftc.teamcode.active.features.Potentiometer
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.robot.robot

@Autonomous
class Autonomous : LinearOpMode() {

    private val leftAction = actionSequenceOf(
        turnTo(19.0, 0.3) then wait(100),
        drive(1050, 0.4),
        turnTo(-19.0, 0.3) then wait(100),
        drive(720, 0.4) then wait(1000),
        drive(-130, 0.3),
        turnTo(-80.0, 0.3) then wait(100),
        drive(710, 0.2),
        turnTo(-120.0, 0.3) then wait(100),
        drive(360, 0.5)
    )

    private val centerAction = actionSequenceOf(
        drive(1801, 0.4),
        drive(-200, 0.4) then wait(100),
        turnTo(-110.0, 0.3) then wait(100)
    )

    private val rightAction = actionSequenceOf(
        turnTo(-19.0, 0.3) then wait(100),
        drive(1050, 0.4),
        turnTo(19.0, 0.3) then wait(100),
        drive(720, 0.4) then wait(1000),
        drive(-130, 0.4),
        turnTo(-110.0, 0.3) then wait(100),
        drive(1800, 0.5)
    )

    private val potentiometerAction = action {
        val potentiometer = requestFeature(Potentiometer)

        val positionChannel = potentiometer.angle.openSubscription()
        val currentPosition = positionChannel.receive()
        positionChannel.cancel()

        when (currentPosition) {
            in 0.0..90.0 -> perform(leftAction)
            in 90.0..180.0 -> perform(centerAction)
            in 180.0..270.0 -> perform(rightAction)
        }
    }

    private val cameraAction = action {
        val detector = requestFeature(CargoPositionDetector).apply { enable() }

        val positionChannel = detector.goldPosition.openSubscription()
        positionChannel.consumeEach {  }

    }

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        val robot = robot(this) {
            install(TankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
            install(Potentiometer)
            install(TankDriveTrain.Localizer)
            install(CargoPositionDetector)
        }


        robot.perform(potentiometerAction)
    }

}


