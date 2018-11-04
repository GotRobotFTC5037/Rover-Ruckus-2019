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
import org.firstinspires.ftc.teamcode.lib.robot.perform
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

    private val leftAction = actionSequenceOf(
        turnTo(-20.0, 1.0) then wait(100),
        drive(1150, 0.4),
        turnTo(20.0, 1.0) then wait(100),
        drive(820, 0.4) then wait(1000)
    )

    private val centerAction = actionSequenceOf(
        drive(2000, 0.6)
    )

    private val rightAction = actionSequenceOf(
        turnTo(20.0, 1.0) then wait(100),
        drive(1150, 0.4),
        turnTo(-20.0, 1.0) then wait(100),
        drive(820, 0.4) then wait(1000)
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
        }.perform {
            val cargoDetector = requestFeature(CargoDetector)
            val position = withTimeoutOrNull(RobotConstants.CARGO_DETECTION_TIMEOUT) {
                cargoDetector.goldPosition.first { it != GoldPosition.UNKNOWN }
            } ?: GoldPosition.UNKNOWN
            val goldAction = when (position) {
                GoldPosition.LEFT -> leftAction
                GoldPosition.CENTER -> centerAction
                GoldPosition.RIGHT -> rightAction
                GoldPosition.UNKNOWN -> throw GoldPositionNotDetectedException()
            }
            perform(goldAction)
        }
    }

}


