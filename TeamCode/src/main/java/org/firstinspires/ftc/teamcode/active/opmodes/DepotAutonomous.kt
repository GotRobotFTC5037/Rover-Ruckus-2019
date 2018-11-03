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
class DepotAutonomous : LinearOpMode() {

    private val raiseLift = move {
        val landerLatch = requestFeature(RobotLift)
        landerLatch.extend()
    }.apply { timeoutMillis = 12000 }

    private val lowerLift = move {
        val landerLatch = requestFeature(RobotLift)
        launch { landerLatch.retract() }
    }

    private val leftAction = actionSequenceOf(
        drive(90,0.4),
        turnTo(20.0) then wait(100),
        drive(950, 0.4),
        turnTo(-20.0) then wait(100),
        drive(850, 0.4) then wait(1000),
        turnTo(-15.0),
        drive(340, 0.3).apply { timeoutMillis = 1000 },
        drive(-120,0.3),
        turnTo(-110.5),
        drive(720,0.4)
    )

    private val centerAction = actionSequenceOf(
        drive(1900, 0.4),
        drive(-500, 0.4),
        turn(-90.0),
        drive(1000, 0.4),
        turn(-40.0),
        drive(1500, 0.7)
    )

    private val rightAction = actionSequenceOf(
        drive(90,0.4),
        turnTo(-20.0) then wait(100),
        drive(950, 0.4),
        turnTo(21.0) then wait(100),
        drive(850, 0.4) then wait(1000),
        turnTo(15.0),
        drive(340, 0.3).apply { timeoutMillis = 1000},
        drive(-120,0.3),
        turnTo(-110.5),
        drive(720,0.4)

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


