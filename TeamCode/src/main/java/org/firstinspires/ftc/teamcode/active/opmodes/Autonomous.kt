@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.first
import org.firstinspires.ftc.teamcode.active.*
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.DriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain

private val extendLift = action {
    val landerLatch = requestFeature(Lift)
    val driveTrain = requestFeature(DriveTrain::class)
    val extendingJob = launch {
        telemetry.log().add("Extending lift")
        landerLatch.extend()
    }
    while (landerLatch.liftPosition < LIFT_DOWN_POSITION - 2000) {
        yield()
    }
    when (driveTrain) {
        is TankDriveTrain -> driveTrain.setMotorPowers(0.35, 0.35)
    }
    extendingJob.join()
    driveTrain.stop()
}

private val retractLift = action {
    val landerLatch = requestFeature(Lift)
    robot.opmodeScope.launch {
        delay(1000)
        landerLatch.retract()
    }
}

private val deliverMarker = action {
    val markerDeployer = requestFeature(MarkerDeployer)
    delay(1000)
    markerDeployer.deploy()
    delay(1000)
    markerDeployer.retract()
    delay(1000)
}

private fun mainAction(leftAction: Action, centerAction: Action, rightAction: Action) = action {
    val telemetry = robot.linearOpMode.telemetry
    val cargoDetector = requestFeature(CargoDetector)
    val position = withTimeoutOrNull(2500) {
        cargoDetector.goldPosition.first { it != GoldPosition.UNKNOWN }
    } ?: GoldPosition.UNKNOWN
    val goldAction = when (position) {
        GoldPosition.LEFT -> {
            telemetry.log().add("Detected left position.")
            leftAction
        }

        GoldPosition.CENTER -> {
            telemetry.log().add("Detected center position.")
            centerAction
        }

        GoldPosition.RIGHT -> {
            telemetry.log().add("Detected right position.")
            rightAction
        }

        GoldPosition.UNKNOWN -> {
            telemetry.log().add("Failed to detect position.")
            centerAction
        }
    }
    perform(extendLift then retractLift then goldAction)
}


@Autonomous
class DepotAutonomous : LinearOpMode() {

    private val leftAction = actionSequenceOf(
        turnTo(35.0),
        drive(60.0),
        turnTo(-25.0),
        drive(60.0),
        turnTo(0.0),
        deliverMarker,
        turnTo(-40.0),
        drive(-180.0)
    )

    private val centerAction = actionSequenceOf(
        turnTo(0.0),
        drive(80.0),
        deliverMarker,
        drive(-75.0),
        turnTo(85.0),
        drive(45.0),
        turnTo(45.0),
        drive(15.0),
        turnTo(125.0),
        drive(70.0)
    )

    private val rightAction = actionSequenceOf(
        turnTo(-35.0),
        drive(70.0),
        turnTo(32.0),
        drive(40.0),
        deliverMarker,
        drive(-115.0),
        turnTo(85.0),
        drive(180.0),
        turnTo(130.0),
        drive(50.0)
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@DepotAutonomous, this)
            .perform(mainAction(leftAction, centerAction, rightAction))
    }

}


@Autonomous
class CraterAutonomous : LinearOpMode() {

    private val leftAction = actionSequenceOf(
        turnTo(CraterAutonomous.InitialTurnHeading),
        drive(CraterAutonomous.InitialSideDriveDistance),
        drive(-25.0),
        turnTo(87.5),
        drive(85.0),
        turnTo(128.0),
        drive(70.0),
        deliverMarker,
        drive(-200.0)
    )

    private val centerAction = actionSequenceOf(
        turnTo(0.0),
        drive(CraterAutonomous.InitialCenterDriveDistance),
        drive(-15.0),
        turnTo(87.5),
        drive(85.0),
        turnTo(128.0),
        drive(100.0),
        deliverMarker,
        drive(-200.0)
    )

    private val rightAction = actionSequenceOf(
        turnTo(-CraterAutonomous.InitialTurnHeading),
        drive(CraterAutonomous.InitialSideDriveDistance),
        drive(-30.0),
        turnTo(87.5),
        drive(100.0),
        turnTo(130.0),
        drive(70.0),
        deliverMarker,
        drive(-200.0)
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@CraterAutonomous, this)
            .perform(mainAction(leftAction, centerAction, rightAction))
    }

    companion object {
        const val InitialTurnHeading = 27.5
        const val InitialCenterDriveDistance = 31.0
        const val InitialSideDriveDistance = 50.0
    }

}
