package org.firstinspires.ftc.teamcode.active.features

import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.util.Range
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.isActive
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.CoroutineContext

/**
 * A [Feature] that gives the angle that a REV Robotics Potentiometer is at.
 */
class Potentiometer(
    private val analogInput: AnalogInput,
    override val coroutineContext: CoroutineContext
) : Feature, CoroutineScope {

    private fun CoroutineScope.broadcastAngle(ticker: ReceiveChannel<Unit>) =
        broadcast(capacity = Channel.CONFLATED) {
            while (isActive) {
                ticker.receive()
                val angle =
                    Range.clip(analogInput.voltage / analogInput.maxVoltage * 270, 0.0, 270.0)
                send(angle)
            }
        }

    val angle: BroadcastChannel<Double> =
        broadcastAngle(ticker(10, mode = TickerMode.FIXED_DELAY))

    class Configuration : FeatureConfiguration {
        val name: String = "potentiometer"
    }

    companion object Installer : FeatureInstaller<Configuration, Potentiometer> {
        override fun install(
            robot: Robot,
            coroutineContext: CoroutineContext,
            configure: Configuration.() -> Unit
        ): Potentiometer {
            val configuration = Configuration().apply(configure)
            val analogInput = robot.hardwareMap.get(AnalogInput::class.java, configuration.name)
            return Potentiometer(
                analogInput,
                coroutineContext
            )
        }
    }

}