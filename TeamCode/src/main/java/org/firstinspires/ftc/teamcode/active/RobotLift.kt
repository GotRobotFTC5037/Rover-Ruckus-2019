@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.hardware.DcMotor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.isActive
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot

private const val LIFT_DOWN_POSITION = -24_600

class RobotLift(private val liftMotor: DcMotor, coroutineScope: CoroutineScope) : Feature {

    val liftPosition: BroadcastChannel<Int> =
        coroutineScope.broadcastLiftPosition(ticker(10, mode = TickerMode.FIXED_DELAY))

    private fun CoroutineScope.broadcastLiftPosition(ticker: ReceiveChannel<Unit>) =
        broadcast(capacity = Channel.CONFLATED) {
            while (isActive) {
                ticker.receive()
                send(liftMotor.currentPosition)
            }
        }

    fun setPower(power: Double) {
        liftMotor.power = power
    }

    suspend fun retract() {
        val positionChannel = liftPosition.openSubscription()
        liftMotor.power = -1.0
        for (position in positionChannel) {
            if (position <= 0) {
                positionChannel.cancel()
            }
        }
    }

    suspend fun extend() {
        val positionChannel = liftPosition.openSubscription()
        liftMotor.power = -1.0
        for (position in positionChannel) {
            if (position >= LIFT_DOWN_POSITION) {
                positionChannel.cancel()
            }
        }
    }

    class Configuration : FeatureConfiguration {
        var liftMotorName: String = "lift"
    }

    companion object Installer : FeatureInstaller<Configuration, RobotLift> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): RobotLift {
            val configuration = Configuration().apply(configure)
            val liftMotor = robot.hardwareMap.get(DcMotor::class.java, configuration.liftMotorName)
            liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            liftMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            return RobotLift(liftMotor, robot)
        }
    }
}