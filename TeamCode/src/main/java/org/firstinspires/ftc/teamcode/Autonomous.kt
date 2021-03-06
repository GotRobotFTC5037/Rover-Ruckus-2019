@file:Suppress("unused")

package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import us.gotrobot.grbase.action.*
import us.gotrobot.grbase.feature.toggleHeadingCorrection
import us.gotrobot.grbase.opmode.RobotOpMode
import us.gotrobot.grbase.robot.Robot
import us.gotrobot.grbase.robot.install
import us.gotrobot.grbase.robot.robot

fun detectGoldPosition(timeout: Long) = action {
    val cargoDetector = feature(CargoDetector)
    val detectedGoldPosition = feature(DetectedGoldPosition)
    detectedGoldPosition.detectedGoldPosition =
        withTimeoutOrNull(timeout) { cargoDetector.goldPosition.firstKnownPosition() }
            ?: CargoDetector.GoldPosition.UNKNOWN
    GlobalScope.launch {
        cargoDetector.shutdown()
    }
}

fun cargoConditionalAction(left: Action, center: Action, right: Action) = action {
    when (feature(DetectedGoldPosition).detectedGoldPosition) {
        CargoDetector.GoldPosition.LEFT -> perform(left)
        CargoDetector.GoldPosition.CENTER -> perform(center)
        CargoDetector.GoldPosition.RIGHT -> perform(right)
        else -> throw RuntimeException("This should never happen")
    }
}

private const val GOLD_DETECTION_TIMEOUT = 1500L

@Autonomous(name = "Depot", group = "0_competitive")
class DepotAutonomous : RobotOpMode() {

    override val action: Action = actionSequenceOf(
        detectGoldPosition(GOLD_DETECTION_TIMEOUT),
        extendLift(),
        timeDrive(time = 200, power = 0.2),
        biasedLateralDrive(distance = 20.0, bias = 0.05) with constantPower(0.35),
        toggleHeadingCorrection(),
        cargoConditionalAction(
            left = actionSequenceOf(
                linearDrive(distance = 35.0) with constantPower(0.35),
                lateralDrive(distance = -75.0),
                linearDrive(distance = 60.0),
                turnTo(heading = -30.0) with constantPower(0.35),
                linearDrive(distance = 40.0),
                releaseMarker(),
                turnTo(-45.0),
                lateralDrive(-40.0) with constantPower(0.35),
                linearDrive(distance = -185.0),
                driveForever(power = -0.2)
            ),
            center = actionSequenceOf(
                lateralDrive(-20.0) with constantPower(0.35),
                linearDrive(distance = 165.0),
                releaseMarker(),
                linearDrive(distance = -75.0) with constantPower(0.5),
                turnTo(heading = 90.0) with constantPower(0.35),
                linearDrive(distance = 130.0),
                turnTo(heading = 135.0) with constantPower(0.25),
                lateralDrive(distance = 35.0),
                driveForever(power = 0.2)
            ),
            right = actionSequenceOf(
                linearDrive(distance = 35.0) with constantPower(0.35),
                lateralDrive(distance = 65.0),
                linearDrive(distance = 65.0),
                turnTo(45.0) with constantPower(0.35),
                linearDrive(30.0),
                releaseMarker(),
                linearDrive(-100.0),
                turnTo(heading = 90.0) with constantPower(0.35),
                linearDrive(distance = 250.0),
                turnTo(heading = 135.0) with constantPower(0.25),
                lateralDrive(distance = 35.0),
                driveForever(power = 0.2)
            )
        )
    )

    override suspend fun robot() = Metabot()

}

@Autonomous(name = "Crater", group = "0_competitive")
class CraterAutonomous : RobotOpMode() {

    override val action = actionSequenceOf(
        detectGoldPosition(GOLD_DETECTION_TIMEOUT),
        extendLift(),
        timeDrive(time = 200, power = 0.2),
        biasedLateralDrive(distance = 20.0, bias = 0.05) with constantPower(0.35),
        toggleHeadingCorrection(),
        cargoConditionalAction(
            left = actionSequenceOf(
                linearDrive(distance = 30.0),
                lateralDrive(-65.0),
                linearDrive(30.0) with constantPower(0.35),
                linearDrive(distance = -15.0) with constantPower(0.35),
                lateralDrive(distance = -90.0)
            ),
            center = actionSequenceOf(
                lateralDrive(-20.0) with constantPower(0.35),
                linearDrive(distance = 85.0) with constantPower(0.35),
                linearDrive(distance = -15.0) with constantPower(0.35),
                lateralDrive(distance = -140.0)
            ),
            right = actionSequenceOf(
                linearDrive(distance = 30.0),
                lateralDrive(50.0),
                linearDrive(30.0) with constantPower(0.35),
                linearDrive(distance = -15.0) with constantPower(0.35),
                lateralDrive(distance = -160.0)
            )
        ),
        turnTo(heading = 135.0),
        lateralDrive(distance = 50.0) with constantPower(0.55),
        linearDrive(distance = 130.0) with constantPower(0.80),
        releaseMarker(),
        lateralDrive(-10.0) with constantPower(0.25),
        linearDrive(distance = -175.0),
        driveForever(power = -0.2)
    )

    override suspend fun robot() = Metabot()

}

@Autonomous(name = "Retract Lift", group = "1_tools")
class RetractLift : RobotOpMode() {
    override val action: Action = lowerLift()
    override suspend fun robot(): Robot = robot {
        install(RobotLift) {
            this.liftMotorName = Metabot.LIFT_MOTOR
            this.limitSwitchName = Metabot.LIMIT_SWITCH
        }
        install(MarkerDeployer) {
            this.servoName = Metabot.MARKER_DEPLOYER_SERVO
        }
    }
}