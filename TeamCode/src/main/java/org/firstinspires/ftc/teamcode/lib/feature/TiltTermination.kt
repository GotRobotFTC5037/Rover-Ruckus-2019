package org.firstinspires.ftc.teamcode.lib.feature

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

class TiltTermination(
    private val terminationAngle: Double,
    private val orientationChannel: ReceiveChannel<IMULocalizer.OrientationUpdate>,
    override val coroutineContext: CoroutineContext
) : Feature, CoroutineScope {

    fun start() = launch {
        while (isActive) {
            val orientation = orientationChannel.receive()
            if (abs(orientation.pitch) > terminationAngle || abs(orientation.roll) > terminationAngle) {
                throw TiltTerminationException()
            }
            yield()
        }
    }

    companion object Installer : FeatureInstaller<Configuration, TiltTermination> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): TiltTermination {
            val configuration = Configuration().apply(configure)
            val orientationChannel = robot[IMULocalizer].newOrientationChannel()
            val tiltTermination = TiltTermination(
                configuration.terminationAngle,
                orientationChannel,
                robot.coroutineContext
            )
            tiltTermination.start()
            return tiltTermination
        }
    }

    class Configuration : FeatureConfiguration {
        var terminationAngle: Double = 90.0
    }

}

class TiltTerminationException : RuntimeException("Robot has tilted over! Opmode stopped.")