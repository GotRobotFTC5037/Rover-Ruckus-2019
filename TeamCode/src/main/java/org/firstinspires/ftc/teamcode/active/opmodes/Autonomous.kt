@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.withTimeoutOrNull
import org.firstinspires.ftc.teamcode.active.RobotConstants
import org.firstinspires.ftc.teamcode.active.features.CargoDetector
import org.firstinspires.ftc.teamcode.active.features.GoldPosition
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.feature.objectDetection.Vuforia
import org.firstinspires.ftc.teamcode.lib.robot.perform
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

    class GoldPositionNotDetectedException : RuntimeException("The gold position was not detected.")

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        robot(this) {
            install(TankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
            install(TankDriveTrain.Localizer)
            install(IMULocalizer)
            install(Vuforia) {
                vuforiaLicenseKey = RobotConstants.VUFORIA_KEY
                fillCameraMonitorViewParent = true
            }
            install(CargoDetector) {
                minimumConfidence = RobotConstants.CARGO_DETECTION_MIN_CONFIDENCE
                useObjectTracker = true
            }
        }.perform {
            val cargoDetector = requestFeature(CargoDetector)

            val position = withTimeoutOrNull(RobotConstants.CARGO_DETECTION_TIMEOUT) {
                cargoDetector.goldPosition.first { it != GoldPosition.UNKNOWN }
            } ?: GoldPosition.UNKNOWN
            when (position) {
                GoldPosition.LEFT -> perform(leftAction)
                GoldPosition.CENTER -> perform(centerAction)
                GoldPosition.RIGHT -> perform(rightAction)
                // TODO: Figure out a backup plan if the gold position is not detect.
                GoldPosition.UNKNOWN -> throw GoldPositionNotDetectedException()
            }
        }
    }

}


