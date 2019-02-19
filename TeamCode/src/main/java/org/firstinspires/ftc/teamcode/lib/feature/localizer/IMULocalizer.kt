package org.firstinspires.ftc.teamcode.lib.feature.localizer

import com.qualcomm.hardware.bosch.BNO055IMU
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference
import org.firstinspires.ftc.teamcode.lib.action.properHeading
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.hardwareMap
import kotlin.coroutines.CoroutineContext

/**
 * A localizer that uses the [BNO055IMU] to detect heading.
 */
class IMULocalizer(
    private val imu: BNO055IMU,
    private val reference: AxesReference,
    private val order: AxesOrder,
    private val initialHeading: Double,
    override val coroutineContext: CoroutineContext
) : RobotHeadingLocalizer, CoroutineScope {

    data class OrientationUpdate(
        val heading: Double,
        val pitch: Double,
        val roll: Double
    )

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.broadcastOrientation(ticker: ReceiveChannel<Unit>): BroadcastChannel<OrientationUpdate> {

        return broadcast(capacity = Channel.CONFLATED) {
            while (isActive) {
                ticker.receive()
                val orientation = imu.getAngularOrientation(reference, order, AngleUnit.DEGREES)
                val update = OrientationUpdate(
                    properHeading(orientation.firstAngle.toDouble() + initialHeading),
                    orientation.secondAngle.toDouble(),
                    orientation.thirdAngle.toDouble()
                )
                send(update)
                yield()
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.broadcastHeading(
        orientationChannel: ReceiveChannel<OrientationUpdate>
    ) = broadcast(capacity = Channel.CONFLATED) {
        while (isActive) {
            val orientation = orientationChannel.receive()
            send(orientation.heading)
            yield()
        }
    }

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    private val orientationChannel = broadcastOrientation(ticker(10))

    @ExperimentalCoroutinesApi
    fun newOrientationChannel() = orientationChannel.openSubscription()

    @ExperimentalCoroutinesApi
    fun newHeadingChannel() = broadcastHeading(newOrientationChannel()).openSubscription()

    class Configuration : FeatureConfiguration {
        var imuName: String = "imu"
        var axesReference: AxesReference = AxesReference.INTRINSIC
        var order: AxesOrder = AxesOrder.ZYX
        var initialHeading: Double = 0.0
    }

    companion object Installer : FeatureInstaller<Configuration, IMULocalizer> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): IMULocalizer {
            val configuration = Configuration().apply(configure)
            val imu = robot.hardwareMap.get(BNO055IMU::class.java, configuration.imuName)
            imu.initialize(BNO055IMU.Parameters())
            return IMULocalizer(
                imu,
                configuration.axesReference,
                configuration.order,
                configuration.initialHeading,
                robot.coroutineContext
            )
        }
    }

}