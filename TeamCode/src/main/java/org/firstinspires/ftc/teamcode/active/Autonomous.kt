@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.internal.opmode.TelemetryImpl
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.DriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.perform

private val extendLift = action {
    val landerLatch = requestFeature(RobotLift)
    val driveTrain = requestFeature(DriveTrain::class)
    val positionChannel = landerLatch.liftPosition.openSubscription()
    val extendingJob = launch { landerLatch.extend() }
    for (position in positionChannel) {
        val telemetry = robot.linearOpMode.telemetry
        telemetry.addData("Lift Position", position)

        if (position >= LIFT_DOWN_POSITION - 3000) {
            driveTrain.setPower(0.3, 0.0)
            positionChannel.cancel()
        }
    }
    extendingJob.join()
    driveTrain.stopAllMotors()
}

private val retractLift = action {
    val landerLatch = requestFeature(RobotLift)
    robot.launch {
        landerLatch.retract()
    }
}

private val deliverMarkerAction = action {
    val markerDeployer = requestFeature(MarkerDeployer)
    delay(1000)
    markerDeployer.deploy()
    delay(1000)
    markerDeployer.retract()
    delay(1000)
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
        GoldPosition.UNKNOWN -> centerAction
    }
    perform(extendLift then retractLift then goldAction)
}


@Autonomous
class DepotAutonomous : LinearOpMode() {

    private val leftAction = actionSequenceOf(
        turnTo(15.0, 1.0) then wait(100),
        drive(1400, 0.4),
        turnTo(-25.0, 1.0) then wait(100),
        drive(1150, 0.4),
        deliverMarkerAction
        
    )

    private val centerAction = actionSequenceOf(
        turnTo(0.0,0.80),
        drive(1850, 0.4),
        deliverMarkerAction,
        drive(-300,0.4),
        turnTo(-45.0,1.0)
    )

    private val rightAction = actionSequenceOf(
        turnTo(-20.0, 1.0) then wait(100),
        drive(1500, 0.4),
        turnTo(15.0, 1.0) then wait(100),
        drive(950, 0.4),
        deliverMarkerAction,
        drive(-1000,0.4)
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
        turnTo(20.0, 1.0) then wait(100),
        drive(750, 0.4),
        drive(-100, 0.4)


    )

    private val centerAction = actionSequenceOf(
        drive(600, 0.5),
        drive(-60, 0.4),
        turnTo(90.0, 1.0) then wait(100)

    )

    private val rightAction = actionSequenceOf(
        turnTo(-20.0, 1.0) then wait(100),
        drive(750, 0.4),
        drive(-100, 0.4)

    )

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        roverRuckusRobot(this).perform(
         mainAction(leftAction, centerAction, rightAction)
        )
    }

}

@Autonomous
class RetractLift : LinearOpMode() {
    override fun runOpMode() {
        roverRuckusRobot(this).perform {
            val lift = requestFeature(RobotLift)
            lift.retract()
        }
    }
}
/*@Autonomous
class GyroEye : LinearOpMode() {
    override fun runOpMode() {
        IMULocalizer
        //var telemetry: Telemetry = TelemetryImpl(this)
        val telemetry = robot.linearOpMode.telemetry
        telemetry.addData("Lift Position", position)
    }

}*/
