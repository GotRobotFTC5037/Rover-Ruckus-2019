@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.robot.Robot
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.first
import org.firstinspires.ftc.teamcode.active.RobotConstants
import org.firstinspires.ftc.teamcode.active.features.*
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain

private fun mainAction(leftAction: Action, centerAction: Action, rightAction: Action) = action {
    val telemetry = robot.linearOpMode.telemetry
    val cargoDetector = requestFeature(CargoDetector)
    val position = withTimeoutOrNull(500) {
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
    perform(
        actionSequenceOf(
            extendLift,
            wiggleWheels(1000),
            turnTo(90.75),
            drive(-5.0),
            retractLift,
            turnTo(75.0),
            drive(5.0),
            goldAction
        )
    )
}

private val extendLift = action {
    val landerLatch = requestFeature(Lift)
    telemetry.log().add("Extending lift")
    landerLatch.extend()
}.apply {
    timeoutMillis = 10_000
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
    markerDeployer.deploy()
    delay(1000)
    markerDeployer.retract()
    delay(1000)
}
private val deployMarker = action {
    val markerDeployer = requestFeature(MarkerDeployer)
    markerDeployer.deploy()
}

private fun wiggleWheels(duration: Long) = action {
    val driveTrain = requestFeature(TankDriveTrain)
    val wiggleJob = launch {
        while (isActive) {
            driveTrain.setMotorPowers(TankDriveTrain.MotorPowers(1.0, 1.0))
            delay(100)
            driveTrain.setMotorPowers(TankDriveTrain.MotorPowers(-1.0, 1.0))
            delay(100)
        }
    }
    delay(duration)
    wiggleJob.cancelAndJoin()
    driveTrain.stop()
}


@Autonomous(name = "Depot Autonomous", group = "Competitive")
class DepotAutonomous : LinearOpMode() {

    private val leftGoldWallFollowingData = WallFollowingData(
        90.0,
        6.0,
        0.165,
        RobotConstants.RightRangeSensor
    )

    private val leftAction = actionSequenceOf(
        turnTo(30.0), // Point toward the left cargo
        drive(80.0), // Drive and displace the gold
        turnTo(-15.0), // Turn towards the depot
        drive(35.0), // Drive into the depot
        turnTo(0.0), // Turn toward the inside of the depot
        deliverMarker,
        turnTo(135.0), // Turn towards the left crater
        wallFollowingDrive(leftGoldWallFollowingData),
        deployMarker then timeDrive(0.25, 1000)
    )

    private val centerAction = actionSequenceOf(
        turnTo(0.0), // Point towards the center cargo
        drive(100.0), // Drive, displace the gold and enter the depot
        deliverMarker,
        drive(-65.0), // Back up towards the lander
        turnTo(90.0), // Turn to the left crater
        drive(115.0), // Drive to the left crater
        turnTo(130.0), // Point toward the left crater
        deployMarker then timeDrive(0.25, 1000)
    )

    private val rightAction = actionSequenceOf(
        turnTo(-30.0), // Point toward teh right cargo
        drive(80.0), // Drive and displace the gold
        turnTo(35.0), // Turn towards the depot
        drive(50.0), // Drive into the depot
        deliverMarker,
        drive(-105.0), // Back out of the depot
        turnTo(90.0), // Turn towards the left crater
        drive(185.0), // Drive to the left crater
        turnTo(130.0), // Turn to the left crater
        deployMarker then timeDrive(0.25, 1000)
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@DepotAutonomous, this)
            .perform(mainAction(leftAction, centerAction, rightAction))
    }

}


@Autonomous(name = "Crater Autonomous", group = "Competitive")
class CraterAutonomous : LinearOpMode() {

    private val depotWallFollowingData = WallFollowingData(
        55.0,
        8.5,
        0.165,
        RobotConstants.RightRangeSensor
    )

    private val craterWallFollowingData = WallFollowingData(
        -150.0,
        6.0,
        0.165,
        RobotConstants.RightRangeSensor
    )

    private val leftAction = actionSequenceOf(
        turnTo(35.0),
        drive(60.0),
        turnTo(0.0),
        drive(-17.5),
        turnTo(90.0),
        drive(60.0),
        turnTo(132.5),
        wallFollowingDrive(depotWallFollowingData),
        deliverMarker,
        wallFollowingDrive(craterWallFollowingData),
        timeDrive(-0.25, 1000)
    )

    private val centerAction = actionSequenceOf(
        turnTo(0.0), // Point towards the center cargo
        drive(43.0), // Displace the cargo
        drive(-12.5), // Back towards the lander
        turnTo(90.0), // Turn to the left wall
        drive(110.0), // Drive to the left wall
        turnTo(132.5), // Turn towards the depot
        wallFollowingDrive(depotWallFollowingData), // Go to the depot
        deliverMarker,
        wallFollowingDrive(craterWallFollowingData), // Go to the crater
        timeDrive(-0.25, 1000)
    )

    private val rightAction = actionSequenceOf(
        turnTo(-35.0),
        drive(60.0),
        drive(-17.5),
        turnTo(90.0),
        drive(127.5),
        turnTo(137.5),
        wallFollowingDrive(depotWallFollowingData),
        deliverMarker,
        wallFollowingDrive(craterWallFollowingData),
        timeDrive(-0.25, 1000)
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@CraterAutonomous, this)
            .perform(mainAction(leftAction, centerAction, rightAction))
    }

}

