package org.firstinspires.ftc.teamcode.lib.feature.localizer

import com.qualcomm.hardware.bosch.BNO055IMU
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.yield
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference
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
    override val coroutineContext: CoroutineContext
) : RobotHeadingLocalizer, CoroutineScope {

    data class OrientationUpdate(
        val heading: Double,
        val pitch: Double,
        val roll: Double
    )

    private fun CoroutineScope.broadcastOrientation() =
        broadcast(capacity = Channel.CONFLATED) {
            while (true) {
                val orientation = imu.getAngularOrientation(reference, order, AngleUnit.DEGREES)
                val update = OrientationUpdate(
                    orientation.firstAngle.toDouble(),
                    orientation.secondAngle.toDouble(),
                    orientation.thirdAngle.toDouble()
                )
                send(update)
                yield()
            }
        }

    private fun CoroutineScope.broadcastHeading(
        orientationChannel: ReceiveChannel<OrientationUpdate>
    ) = broadcast(capacity = Channel.CONFLATED) {
        while (true) {
            val orientation = orientationChannel.receive()
            send(orientation.heading)
            yield()
        }
    }

    private val orientationChannel = broadcastOrientation()

    fun newOrientationChannel() = orientationChannel.openSubscription()

    fun newHeadingChannel() = broadcastHeading(newOrientationChannel()).openSubscription()

    class Configuration : FeatureConfiguration {
        var imuName: String = "imu"
        var axesReference: AxesReference = AxesReference.INTRINSIC
        var order: AxesOrder = AxesOrder.ZYX
    }

    companion object Installer :
        FeatureInstaller<Configuration, IMULocalizer> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): IMULocalizer {
            val configuration = Configuration().apply(configure)
            val imu = robot.hardwareMap.get(BNO055IMU::class.java, configuration.imuName)
            imu.initialize(BNO055IMU.Parameters())
            return IMULocalizer(
                imu,
                configuration.axesReference,
                configuration.order,
                robot.coroutineContext
            )
        }
    }

}