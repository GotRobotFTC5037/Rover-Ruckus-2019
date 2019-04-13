package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import us.gotrobot.grbase.action.*
import us.gotrobot.grbase.opmode.RobotOpMode

@Autonomous(name = "Depot")
class DepotAutonomous : RobotOpMode() {

    override val action = actionSequenceOf(
        timeDrive(time = 500, power = 0.2),
        lateralDrive(distance = 25.0),
        linearDrive(distance = 190.0),
        releaseMarker(),
        linearDrive(distance = -100.0),
        turnTo(heading = 90.0),
        linearDrive(distance = 175.0),
        turnTo(heading = 135.0),
        lateralDrive(distance = 0.25),
        driveForever(power = 0.2)
    )

    override suspend fun robot() = Metabot()

}

@Autonomous(name = "Crater")
class CraterAutonomous : RobotOpMode() {

    override val action = actionSequenceOf(
        timeDrive(time = 500, power = 0.2),
        lateralDrive(distance = 25.0),
        linearDrive(distance = 80.0),
        linearDrive(distance = -25.0),
        lateralDrive(distance = -160.0),
        turnTo(heading = 135.0),
        lateralDrive(distance = 25.0),
        linearDrive(distance = 150.0),
        releaseMarker(),
        linearDrive(distance = -175.0),
        driveForever(power = -0.2)
    )

    override suspend fun robot() = Metabot()

}