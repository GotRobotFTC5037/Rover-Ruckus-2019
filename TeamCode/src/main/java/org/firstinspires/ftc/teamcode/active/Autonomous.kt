@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.firstinspires.ftc.teamcode.lib.action.*

private val raiseLift = action {
    val landerLatch = requestFeature(RobotLift)
    landerLatch.extend()
}.apply {
    timeoutMillis = 12000
    disabled = true
}

private val lowerLift = action {
    val landerLatch = requestFeature(RobotLift)
    launch { landerLatch.retract() }
}.apply {
    disabled = true
}

private val deliverMarkerAction = action {
    val markerDeployer = requestFeature(MarkerDeployer)
    markerDeployer.deploy()
    launch {
        delay(1000)
        markerDeployer.retract()
    }
}.apply {
    disabled = true
}

private fun mainAction(leftAction: Action, centerAction: Action, rightAction: Action) = action {
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

class GoldPositionNotDetectedException : RuntimeException("The gold position was not detected.")

@Autonomous
class DepotAutonomous : LinearOpMode() {

    private val leftAction = actionSequenceOf(
        drive(90, 0.4),
        turnTo(20.0, 1.0) then wait(100),
        drive(1150, 0.4),
        turnTo(-20.0, 1.0) then wait(100),
        drive(820, 0.4),
        deliverMarkerAction,
        drive(-180, 0.4),
        turnTo(-80.0, 1.0) then wait(100),
        drive(720, 0.4),
        turnTo(-120.0, 1.0) then wait(100),
        drive(1500, 0.5)
    )

    private val centerAction = actionSequenceOf(
        drive(1900, 0.4),
        drive(-85, 0.4),
        turnTo(-90.0, 1.0),
        deliverMarkerAction,
        drive(720, 0.4),
        turnTo(-110.0, 1.0),
        drive(1500, 0.7)
    )

    private val rightAction = actionSequenceOf(
        drive(90, 0.4),
        turnTo(-20.0, 1.0) then wait(100),
        drive(1150, 0.4),
        turnTo(20.0, 1.0) then wait(100),
        drive(820, 0.4),
        deliverMarkerAction,
        drive(-180, 0.4),
        turnTo(-90.0, 1.0) then wait(100),
        drive(725, 0.4),
        turnTo(-110.0, 1.0) then wait(100),
        drive(1500, 0.5)
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        roverRuckusRobot(this).perform(
            actionSequenceOf(
                raiseLift,
                lowerLift,
                mainAction(leftAction, centerAction, rightAction)
            )
        )
    }

}


@Autonomous
class CraterAutonomous : LinearOpMode() {

    private val leftAction = actionSequenceOf(
        drive(5, 0.4),
        turnTo(25.0, 1.0),
        drive(750, 0.4),
        drive(-100, 0.4),
        turnTo(75.0, 1.0),
        drive(820, 0.4),
        turnTo(110.0, 1.0),
        drive(800, 0.5),
        deliverMarkerAction,
        drive(-1500, 0.7)
    )

    private val centerAction = actionSequenceOf(
        drive(550, 0.6).apply { timeoutMillis = 1000 },
        drive(-80, 0.3),
        turnTo(72.5, 1.0),
        drive(900, 0.4),
        turnTo(115.0, 1.0),
        drive(600, 0.4),
        deliverMarkerAction,
        drive(-2000, 0.7)
    )

    private val rightAction = actionSequenceOf(
        drive(5, 0.4),
        turnTo(-25.0, 1.0),
        drive(750, 0.4),
        drive(-100, 0.4),
        turnTo(75.0, 1.0),
        drive(1000, 0.4),
        turnTo(110.0, 1.0),
        drive(1800, 0.5),
        deliverMarkerAction,
        drive(-1500, 0.7)
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        roverRuckusRobot(this).perform(
            actionSequenceOf(
                raiseLift,
                lowerLift,
                mainAction(leftAction, centerAction, rightAction)
            )
        )
    }

}