package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.hardware.bosch.BNO055IMU
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.TickerMode
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.channels.ticker
import kotlinx.coroutines.experimental.isActive
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference
import org.firstinspires.ftc.robotcore.external.navigation.Orientation

data class RobotPosition(
    val linearPosition: Double,
    val lateralPosition: Double
)

/**
 * Reports the current heading of the robot.
 */
interface RobotHeadingLocalizer {

    /**
     * Returns a [ReceiveChannel] with the current heading of the robot.
     */
    fun newHeadingChannel(): ReceiveChannel<Double>
}

/**
 * Reports the position of the robot.
 */
interface RobotPositionLocalizer {

    fun newPositionChannel(): ReceiveChannel<RobotPosition>

}

/**
 * A localizer that uses the [BNO055IMU] to detect heading.
 */
class IMULocalizer(
    private val imu: BNO055IMU,
    private val coroutineScope: CoroutineScope
) : RobotHeadingLocalizer {

    val isCalibrated get() = imu.isGyroCalibrated

    private fun CoroutineScope.orientation(ticker: ReceiveChannel<Unit>) = produce<Orientation> {
        while (isActive) {
            val orientation = imu.getAngularOrientation(
                AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES
            )
            ticker.receive()
            offer(orientation)
        }
        ticker.cancel()
    }

    private fun CoroutineScope.heading(orientation: ReceiveChannel<Orientation>) = produce<Double> {
        while (isActive) {
            val robotOrientation = orientation.receive()
            offer(robotOrientation.firstAngle.toDouble())
        }
        orientation.cancel()
    }

    override fun newHeadingChannel(): ReceiveChannel<Double> {
        val ticker = ticker(delay = 10, mode = TickerMode.FIXED_DELAY)
        val orientation = coroutineScope.orientation(ticker)
        return coroutineScope.heading(orientation)
    }

    /**
     * Configures a [IMULocalizer].
     *
     * @param coroutineScope The [CoroutineScope] that the channels should run on.
     */
    class Configuration(val coroutineScope: CoroutineScope) : RobotFeatureConfiguration {

        /**
         * The name of the imu as used in the configuration file.
         */
        var imuName: String = "imu"
    }

    companion object Feature : RobotFeature<Configuration, IMULocalizer> {

        override val key: RobotFeatureKey<IMULocalizer> = RobotFeatureKey("IMULocalizer")

        override fun install(robot: Robot, configure: Configuration.() -> Unit): IMULocalizer {
            // TODO: Find a more elegant way to get a coroutine scope. For the moment, [RobotImpl] is our only subclass of [Robot] and it implements [CoroutineScope], but that may not always be the case.
            val configuration = Configuration(robot as CoroutineScope).apply(configure)
            val imu = robot.hardwareMap.get(BNO055IMU::class.java, configuration.imuName)
                .apply { initialize(BNO055IMU.Parameters()) }
            return IMULocalizer(imu, configuration.coroutineScope)
        }

    }

}