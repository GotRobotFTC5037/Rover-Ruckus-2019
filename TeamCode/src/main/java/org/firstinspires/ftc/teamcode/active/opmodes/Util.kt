@file:Suppress("unused")

package org.firstinspires.ftc.teamcode.active.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.teamcode.active.features.Lift
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.install
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.robot.robot
import org.firstinspires.ftc.teamcode.lib.util.Utility

@Autonomous(name = "Retract Lift", group = "Utils")
class RetractLift : LinearOpMode(), Utility {
    override fun runOpMode() = runBlocking {
        robot(this@RetractLift, this) {
            install(Lift) {
                liftMotorName = "lift motor"
            }
        }.perform {
            val lift = requestFeature(Lift)
            lift.retract()
        }
    }
}
