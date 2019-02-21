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
            turnTo(91.5),
            drive(-2.0),
            retractLift,
            turnTo(90.0),
            drive(2.0),
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

//private val deployIntake = action {
//    val intake = requestFeature(CargoDeliverySystem.Intake)

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
    private val leftAction = actionSequenceOf(
        turnTo(40.0),
        drive(80.0),
        turnTo(-20.0),
        drive(70.0),
        turnTo(0.0),
        deliverMarker,
        turnTo(135.0),
        wallFollowingDrive(WallFollowingData(110.0, 7.5, 0.165, RobotConstants.RightRangeSensor)),
        deployMarker
    )

    private val centerAction = actionSequenceOf(
        turnTo(12.5),
        drive(100.0),
        deliverMarker,
        turnTo(0.0),
        drive(-80.0),
        turnTo(90.0),
        drive(115.0),
        turnTo(130.0),
        deployMarker
    )

    private val rightAction = actionSequenceOf(
        turnTo(-20.0),
        drive(70.0),
        turnTo(35.0),
        drive(50.0),
        deliverMarker,
        drive(-105.0),
        turnTo(90.0),
        drive(180.0),
        turnTo(130.0),
        drive(40.0),
        deployMarker
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@DepotAutonomous, this)
            .perform(mainAction(leftAction, centerAction, rightAction))
    }

}


@Autonomous(name = "Crater Autonomous", group = "Competitive")
class CraterAutonomous : LinearOpMode() {

    private val leftAction = actionSequenceOf(
        turnTo(45.5),
        drive(65.0),
        turnTo(0.0),
        drive(-17.5),
        turnTo(90.0),
        drive(70.0),
        turnTo(132.5),
        wallFollowingDrive(WallFollowingData(55.0, 8.5, 0.165, RobotConstants.RightRangeSensor)),
        deliverMarker,
        wallFollowingDrive(WallFollowingData(-130.0, 8.5, 0.165, RobotConstants.RightRangeSensor))
    )

    private val centerAction = actionSequenceOf(
        turnTo(20.0),
        drive(43.0),
        turnTo(0.0),
        drive(-17.5),
        turnTo(90.0),
        drive(120.0),
        turnTo(132.5),
        wallFollowingDrive(WallFollowingData(55.0, 8.5, 0.165, RobotConstants.RightRangeSensor)),
        deliverMarker,
        wallFollowingDrive(WallFollowingData(-130.0, 8.5, 0.165, RobotConstants.RightRangeSensor))
    )   

    private val rightAction = actionSequenceOf(
        turnTo(-25.0),
        drive(50.0),
        drive(-17.5),
        turnTo(90.0),
        drive(155.0),
        turnTo(132.5),
        wallFollowingDrive(WallFollowingData(55.0, 8.5, 0.165, RobotConstants.RightRangeSensor)),
        deliverMarker,
        wallFollowingDrive(WallFollowingData(-130.0, 8.5, 0.165, RobotConstants.RightRangeSensor))
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@CraterAutonomous, this)
            .perform(mainAction(leftAction, centerAction, rightAction))
    }

}

