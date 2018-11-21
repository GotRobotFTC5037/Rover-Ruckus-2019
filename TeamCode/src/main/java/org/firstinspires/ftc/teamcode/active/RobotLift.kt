@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.TouchSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.isActive
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot

const val LIFT_DOWN_POSITION = 26_000

class RobotLift(
    private val liftMotor: DcMotor,
    val liftButton: TouchSensor,
    coroutineScope: CoroutineScope
) : Feature {

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
        if (power < 0.0 && !liftButton.isPressed) {
            liftMotor.power = power
        } else if (power >= 0.0) {
            liftMotor.power = power
        }
    }

    suspend fun retract() {
        val positionChannel = liftPosition.openSubscription()
        liftMotor.power = -1.0
        for (position in positionChannel) {
            if (liftButton.isPressed) {
                liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
                liftMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                positionChannel.cancel()
            }
        }
        liftMotor.power = 0.0
    }

    suspend fun extend() {
        val positionChannel = liftPosition.openSubscription()
        liftMotor.power = 1.0
        for (position in positionChannel) {
            if (position >= LIFT_DOWN_POSITION) {
                positionChannel.cancel()
            }
        }
        liftMotor.power = 0.0
    }

    class Configuration : FeatureConfiguration {
        var liftMotorName: String = "lift motor"
        var liftButton: String = "lift button"
    }

    companion object Installer : FeatureInstaller<Configuration, RobotLift> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): RobotLift {
            val config = Configuration().apply(configure)

            val liftMotor = robot.hardwareMap.get(DcMotor::class.java, config.liftMotorName)
            liftMotor.direction = DcMotorSimple.Direction.REVERSE
            liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            liftMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

            val liftButton = robot.hardwareMap.get(TouchSensor::class.java, config.liftButton)

            return RobotLift(liftMotor, liftButton, robot)
        }
    }
}