package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.runBlocking
import org.firstinspires.ftc.teamcode.active.MarkerDeployer
import org.firstinspires.ftc.teamcode.active.RobotLift
import org.firstinspires.ftc.teamcode.active.roverRuckusRobot
import org.firstinspires.ftc.teamcode.lib.action.action
import org.firstinspires.ftc.teamcode.lib.driverControl
import org.firstinspires.ftc.teamcode.lib.feature.TankDriveTrain

class ExperimantalTeleOp : LinearOpMode() {

    private val teleOp = action {
        val driveTrain = requestFeature(TankDriveTrain)
        val lift = requestFeature(RobotLift)
        val deployer = requestFeature(MarkerDeployer)

        driverControl {

        }
    }

    override fun runOpMode() = runBlocking {
        roverRuckusRobot(this@ExperimantalTeleOp, this).perform(teleOp)
    }
}