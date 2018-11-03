@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.firstinspires.ftc.teamcode.active.RobotConstants
import org.firstinspires.ftc.teamcode.active.features.CargoDetector
import org.firstinspires.ftc.teamcode.active.features.GoldPosition
import org.firstinspires.ftc.teamcode.active.features.RobotLift
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.feature.objectDetection.Vuforia
import org.firstinspires.ftc.teamcode.lib.robot.robot

@Autonomous
class CraterAutonomous : LinearOpMode() {

    private val raiseLift = move {
        val landerLatch = requestFeature(RobotLift)
        landerLatch.extend()
    }.apply { timeoutMillis = 12000 }

    private val lowerLift = move {
        val landerLatch = requestFeature(RobotLift)
        launch { landerLatch.retract() }
    }

    private val mainAction = action {
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

    private val leftAction = actionSequenceOf(
        turnTo(20.0) then wait(100),
        drive(1150, 0.4),
        turnTo(-20.0) then wait(100),
        drive(820, 0.4) then wait(1000),
        drive(-180, 0.3),
        turnTo(-80.0) then wait(100),
        drive(710, 0.2),
        turnTo(-120.0) then wait(100),
        drive(360, 0.5)
    )

    private val centerAction = actionSequenceOf(
        drive(1700, 0.4),
        drive(-420, 0.4),
        turn(-90.0),
        drive(420, 0.4),
        turn(-45.0),
        drive(15/*00*/, 0.7)
    )

    private val rightAction = actionSequenceOf(
        turnTo(-20.0) then wait(100),
        drive(1150, 0.4),
        turnTo(20.0) then wait(100),
        drive(820, 0.4) then wait(1000),
        drive(-180, 0.3),
        turnTo(80.0) then wait(100),
        drive(710, 0.2),
        turnTo(120.0) then wait(100),
        drive(360, 0.5)
    )

    class GoldPositionNotDetectedException : RuntimeException("The gold position was not detected.")

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        robot(this) {
            install(TankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
            install(RobotLift) {
                liftMotorName = "lift"
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
        }.perform(/*raiseLift then lowerLift then*/ mainAction)
    }

}


