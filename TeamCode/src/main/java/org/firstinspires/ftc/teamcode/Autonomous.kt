package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import us.gotrobot.grbase.action.*
import us.gotrobot.grbase.feature.toggleHeadingCorrection
import us.gotrobot.grbase.opmode.RobotOpMode
import us.gotrobot.grbase.robot.Robot

@Autonomous(name = "Depot", group = "competitive")
class DepotAutonomous : RobotOpMode() {

    override val action = actionSequenceOf(
//        extendLift(),
        timeDrive(time = 200, power = 0.2),
        lateralDrive(distance = 5.0) with power(0.25),
        toggleHeadingCorrection(),
        linearDrive(distance = 140.0),
//        async(lowerLift()),
        releaseMarker(),
        linearDrive(distance = -50.0) with power(0.5),
        turnTo(heading = 90.0) with power(0.35),
        linearDrive(distance = 130.0),
        turnTo(heading = 135.0) with power(0.25),
        lateralDrive(distance = 35.0).apply { context[PowerManager] = power(0.25); timeoutMillis = 1500 },
        driveForever(power = 0.2)
    )

    override suspend fun robot() = Metabot()

}

@Autonomous(name = "Crater", group = "competitive")
class CraterAutonomous : RobotOpMode() {

    override val action = actionSequenceOf(
//        extendLift(),
        timeDrive(time = 200, power = 0.2),
        lateralDrive(distance = 5.0) with power(0.25),
        toggleHeadingCorrection(),
        linearDrive(distance = 50.0),
//        async(lowerLift()),
        linearDrive(distance = -10.0) with power(0.25),
        lateralDrive(distance = -140.0),
        turnTo(heading = 135.0),
        lateralDrive(distance = 25.0) with power(0.25),
        linearDrive(distance = 130.0),
        releaseMarker(),
        linearDrive(distance = -175.0),
        driveForever(power = -0.2)
    )

    override suspend fun robot() = Metabot()

}

@Autonomous(name = "Retract Lift", group = "Tools")
class RetractLift : RobotOpMode() {
    override val action: Action = lowerLift()
    override suspend fun robot(): Robot = Metabot()
}