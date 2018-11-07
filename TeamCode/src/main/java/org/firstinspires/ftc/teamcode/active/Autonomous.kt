@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.firstinspires.ftc.teamcode.lib.action.*

private val raiseLiftAction = move {
    val landerLatch = requestFeature(RobotLift)
    landerLatch.extend()
}.apply { timeoutMillis = 12000 }

private val lowerLiftAction = move {
    val landerLatch = requestFeature(RobotLift)
    launch { landerLatch.retract() }
}

private val deliverMarkerAction = action {
    //    val markerDeployer = requestFeature(MarkerDeployer)
//    markerDeployer.deploy()
//    launch {
//        delay(1000)
//        markerDeployer.retract()
//    }
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
        turnTo(20.0, 1.0) then wait(100),
        drive(1150, 0.4),
        turnTo(-20.0, 1.0) then wait(100),
        drive(820, 0.4),
        deliverMarkerAction,
        drive(-180, 0.3),
        turnTo(-80.0, 1.0) then wait(100),
        drive(710, 0.2),
        turnTo(-120.0, 1.0) then wait(100),
        drive(360, 0.5)
    )

    private val centerAction = actionSequenceOf(
        drive(1900, 0.4),
        deliverMarkerAction,
        drive(-500, 0.4),
        turn(-90.0, 0.4),
        drive(1000, 0.4),
        turn(-40.0, 0.4),
        drive(1500, 0.7)
    )

    private val rightAction = actionSequenceOf(
        turnTo(-20.0, 1.0) then wait(100),
        drive(1150, 0.4),
        turnTo(20.0, 1.0) then wait(100),
        drive(820, 0.4),
        deliverMarkerAction,
        drive(-180, 0.3),
        turnTo(80.0, 1.0) then wait(100),
        drive(710, 0.2),
        turnTo(120.0, 1.0) then wait(100),
        drive(360, 0.5)
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        roverRuckusRobot(this).perform(
            mainAction(leftAction, centerAction, rightAction)
        )
    }

}


@Autonomous
class CraterAutonomous : LinearOpMode() {

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

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        roverRuckusRobot(this).perform(
            mainAction(leftAction, centerAction, rightAction)
        )
    }

}