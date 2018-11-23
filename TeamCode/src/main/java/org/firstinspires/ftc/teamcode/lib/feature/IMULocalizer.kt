@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.lib.feature

import com.qualcomm.hardware.bosch.BNO055IMU
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.isActive
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.CoroutineContext

/**
 * A localizer that uses the [BNO055IMU] to detect heading.
 */
class IMULocalizer(
    private val imu: BNO055IMU,
    pollRate: Long,
    override val coroutineContext: CoroutineContext
) : RobotHeadingLocalizer, CoroutineScope {

    override val isReady: Boolean
        get() = imu.isGyroCalibrated

    private fun CoroutineScope.broadcastHeading(ticker: ReceiveChannel<Unit>) =
        broadcast(capacity = Channel.CONFLATED) {
            while (isActive) {
                ticker.receive()
                val orientation = imu.getAngularOrientation(
                    AxesReference.INTRINSIC,
                    AxesOrder.ZYX,
                    AngleUnit.DEGREES
                )
                val heading = orientation.firstAngle.toDouble()
                send(heading)
            }
        }

    override val heading: BroadcastChannel<Double> =
        broadcastHeading(ticker(delayMillis = pollRate, mode = TickerMode.FIXED_DELAY))


    /**
     * Configures a [IMULocalizer].
     */
    class Configuration : FeatureConfiguration {

        /**
         * The name of the imu as used in the configuration file.
         */
        var imuName: String = "imu"

        /**
         * The rate at which [IMULocalizer.heading] will produce headings in milliseconds.
         * [BNO055IMU] has a maximum poll rate of 100Hz so it is recommended that this value stay
         * greater than or equal to 10.
         */
        var pollRate: Long = 10
    }

    companion object Installer : FeatureInstaller<Configuration, IMULocalizer> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): IMULocalizer {
            val configuration = Configuration()
                .apply(configure)
            val imu = robot.hardwareMap.get(BNO055IMU::class.java, configuration.imuName)
            imu.initialize(BNO055IMU.Parameters())
            return IMULocalizer(
                imu,
                configuration.pollRate,
                robot.coroutineContext
            )
        }
    }

}