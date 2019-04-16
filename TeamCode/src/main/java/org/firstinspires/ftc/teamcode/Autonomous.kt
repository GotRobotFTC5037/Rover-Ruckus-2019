package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import us.gotrobot.grbase.action.*
import us.gotrobot.grbase.feature.toggleHeadingCorrection
import us.gotrobot.grbase.opmode.RobotOpMode
import us.gotrobot.grbase.robot.Robot

@Autonomous(name = "Depot", group = "_competitive")
class DepotAutonomous : RobotOpMode() {

    override val action = actionSequenceOf(
        extendLift(),
        timeDrive(time = 200, power = 0.2),
        lateralDrive(distance = 20.0) with constantPower(0.25),
        toggleHeadingCorrection(),
        linearDrive(distance = 165.0),
        releaseMarker(),
        linearDrive(distance = -65.0) with constantPower(0.5),
        turnTo(heading = 90.0) with constantPower(0.35),
        linearDrive(distance = 130.0),
        turnTo(heading = 135.0) with constantPower(0.25),
        lateralDrive(distance = 35.0),
        driveForever(power = 0.2)
    )

    override suspend fun robot() = Metabot()

}

@Autonomous(name = "Crater", group = "_competitive")
class CraterAutonomous : RobotOpMode() {

    override val action = actionSequenceOf(
        extendLift(),
        timeDrive(time = 200, power = 0.2),
        lateralDrive(distance = 20.0) with constantPower(0.35),
        toggleHeadingCorrection(),
        linearDrive(distance = 60.0),
        linearDrive(distance = -15.0) with constantPower(0.35),
        lateralDrive(distance = -140.0),
        turnTo(heading = 135.0),
        lateralDrive(distance = 40.0) with constantPower(0.35),
        linearDrive(distance = 130.0) with constantPower(1.00),
        releaseMarker(),
        linearDrive(distance = -175.0),
        driveForever(power = -0.2)
    )

    override suspend fun robot() = Metabot()

}

@Autonomous(name = "Retract Lift", group = "tools")
class RetractLift : RobotOpMode() {
    override val action: Action = lowerLift()
    override suspend fun robot(): Robot = Metabot()
}