@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.withTimeoutOrNull
import org.firstinspires.ftc.teamcode.active.RobotConstants
import org.firstinspires.ftc.teamcode.active.features.CargoDetector
import org.firstinspires.ftc.teamcode.active.features.GoldPosition
import org.firstinspires.ftc.teamcode.active.features.Potentiometer
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.feature.objectDetection.Vuforia
import org.firstinspires.ftc.teamcode.lib.robot.robot

@Autonomous
class Autonomous : LinearOpMode() {

    private val leftAction = actionSequenceOf(
        turnTo(19.0, 1.0) then wait(100),
        drive(1050, 0.4),
        turnTo(-19.0, 1.0) then wait(100),
        drive(720, 0.4) then wait(1000),
        drive(-130, 0.3),
        turnTo(-80.0, 1.0) then wait(100),
        drive(710, 0.2),
        turnTo(-120.0, 1.0) then wait(100),
        drive(360, 0.5)
    )

    private val centerAction = actionSequenceOf(
        drive(1620, 0.55),
        drive(-130, 0.7) then wait(100),
        turnTo(-20.0, 1.0) then wait(100),
        drive(260, 0.5) then timeDrive(-100, 0.5)
    )

    private val rightAction = actionSequenceOf(
        turnTo(-19.0, 1.0) then wait(100),
        drive(1050, 0.4),
        turnTo(19.0, 1.0) then wait(100),
        drive(720, 0.4) then wait(1000),
        drive(-130, 0.4),
        turnTo(-110.0, 1.0) then wait(100),
        drive(1800, 0.5)
    )

    private val potentiometerAction = action {
        val potentiometer = requestFeature(Potentiometer)

        val positionChannel = potentiometer.angle.openSubscription()
        val position = positionChannel.first()
        when (position) {
            in 0.0..90.0 -> perform(leftAction)
            in 90.0..180.0 -> perform(centerAction)
            in 180.0..270.0 -> perform(rightAction)
        }
    }

    private val cameraAction = action {
        val cargoDetector = requestFeature(CargoDetector)

        val positionChannel = cargoDetector.goldPosition.openSubscription()
        val position = withTimeoutOrNull(5000) {
            positionChannel.first { it != GoldPosition.UNKNOWN }
        } ?: GoldPosition.UNKNOWN
        when (position) {
            GoldPosition.LEFT -> perform(leftAction)
            GoldPosition.CENTER -> perform(centerAction)
            GoldPosition.RIGHT -> perform(rightAction)
            GoldPosition.UNKNOWN -> TODO("Undefined action for unknown gold position")
        }
    }

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        val robot = robot(this) {
            install(TankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
            install(TankDriveTrain.Localizer)
            install(IMULocalizer)
            install(Potentiometer)
            install(Vuforia) {
                key = RobotConstants.VUFORIA_KEY
            }
            install(CargoDetector)
        }

        robot.perform(potentiometerAction)
//        robot.perform(cameraAction)
    }

}


