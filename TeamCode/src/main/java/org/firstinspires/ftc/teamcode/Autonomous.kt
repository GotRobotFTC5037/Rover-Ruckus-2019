package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import us.gotrobot.grbase.action.*
import us.gotrobot.grbase.feature.toggleHeadingCorrection
import us.gotrobot.grbase.feature.vision.ObjectDetector
import us.gotrobot.grbase.feature.vision.Vuforia
import us.gotrobot.grbase.opmode.CoroutineOpMode
import us.gotrobot.grbase.opmode.RobotOpMode
import us.gotrobot.grbase.robot.Robot
import us.gotrobot.grbase.robot.install
import us.gotrobot.grbase.robot.robot

fun cargoConditionalAction(left: Action, center: Action, right: Action) = action {
    val cargoDetector = feature(CargoDetector)
    val position = cargoDetector.goldPosition.firstKnownPosition()
    GlobalScope.launch {
        cargoDetector.shutdown()
    }
    when (position) {
        CargoDetector.GoldPosition.LEFT -> perform(left)
        CargoDetector.GoldPosition.CENTER -> perform(center)
        CargoDetector.GoldPosition.RIGHT -> perform(right)
        else -> throw RuntimeException("This should never happen")
    }
}

@Autonomous(name = "Depot", group = "0_competitive")
class DepotAutonomous : RobotOpMode() {

    override val action: Action = actionSequenceOf(
        extendLift(),
        timeDrive(time = 200, power = 0.2),
        biasedLateralDrive(distance = 20.0, bias = 0.25) with constantPower(0.25),
        toggleHeadingCorrection(),
        cargoConditionalAction(
            left = actionSequenceOf(
                linearDrive(distance = 50.0),
                async(lowerLift()),
                lateralDrive(distance = 50.0),
                linearDrive(distance = 75.0),
                turnTo(heading = 45.0),
                linearDrive(distance = 50.0),
                releaseMarker(),
                linearDrive(distance = -150.0),
                turnTo(heading = 90.0),
                linearDrive(distance = 200.0),
                lateralDrive(distance = 35.0),
                driveForever(power = 0.2)
            ),
            center = actionSequenceOf(
                linearDrive(distance = 165.0),
                async(lowerLift()),
                releaseMarker(),
                linearDrive(distance = -75.0) with constantPower(0.5),
                turnTo(heading = 90.0) with constantPower(0.35),
                linearDrive(distance = 130.0),
                turnTo(heading = 135.0) with constantPower(0.25),
                lateralDrive(distance = 35.0),
                driveForever(power = 0.2)
            ),
            right = actionSequenceOf(
                linearDrive(distance = 50.0),
                async(lowerLift()),
                lateralDrive(distance = -50.0),
                linearDrive(distance = 50.0),
                releaseMarker(),
                turnTo(heading = -45.0),
                linearDrive(distance = -150.0),
                driveForever(power = -0.2)
            )
        )
    )

    override suspend fun robot() = Metabot()

}

@Autonomous(name = "Crater", group = "0_competitive")
class CraterAutonomous : RobotOpMode() {

    override val action = actionSequenceOf(
        extendLift(),
        timeDrive(time = 200, power = 0.2),
        biasedLateralDrive(distance = 20.0, bias = 0.25) with constantPower(0.35),
        toggleHeadingCorrection(),
        cargoConditionalAction(
            left = actionSequenceOf(
                linearDrive(distance = 30.0),
                lateralDrive(50.0),
                linearDrive(-30.0),
                linearDrive(distance = -15.0) with constantPower(0.35),
                async(lowerLift()),
                lateralDrive(distance = -190.0)
            ),
            center = actionSequenceOf(
                linearDrive(distance = 60.0),
                linearDrive(distance = -15.0) with constantPower(0.35),
                async(lowerLift()),
                lateralDrive(distance = -140.0)
            ),
            right = actionSequenceOf(
                linearDrive(distance = 30.0),
                lateralDrive(-50.0),
                linearDrive(-30.0),
                linearDrive(distance = -15.0) with constantPower(0.35),
                async(lowerLift()),
                lateralDrive(distance = -90.0)
            )
        ),
        turnTo(heading = 135.0),
        lateralDrive(distance = 40.0) with constantPower(0.35),
        linearDrive(distance = 130.0) with constantPower(1.00),
        releaseMarker(),
        linearDrive(distance = -175.0),
        driveForever(power = -0.2)
    )

    override suspend fun robot() = Metabot()

}

@Autonomous(name = "Retract Lift", group = "1_tools")
class RetractLift : RobotOpMode() {
    override val action: Action = lowerLift()
    override suspend fun robot(): Robot = Metabot()
}

@Autonomous(name = "Camera Test", group = "2_tests")
class CameraTest : CoroutineOpMode() {

    lateinit var robot: Robot

    override suspend fun initialize() {
        robot = robot {
            val vuforia = install(Vuforia) {
                licenceKey = Metabot.VUFORIA_LICENCE_KEY
            }
            val objectDetector = install(ObjectDetector) {
                this.vuforia = vuforia
                this.data = ObjectDetector.AssetData(
                    CargoDetector.TFOD_MODEL_ASSET,
                    listOf(
                        CargoDetector.LABEL_GOLD_MINERAL,
                        CargoDetector.LABEL_SILVER_MINERAL
                    )
                )
            }
            install(CargoDetector) {
                this.objectDetector = objectDetector
            }
        }
    }

    override suspend fun run() = robot.perform(foreverNothing())

}