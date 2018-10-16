package org.firstinspires.ftc.teamcode.lib.feature.localizer

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
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot

/**
 * A localizer that uses the [BNO055IMU] to detect heading.
 */
class RobotIMULocalizer(
    private val imu: BNO055IMU,
    private val coroutineScope: CoroutineScope
) : RobotHeadingLocalizer {

    override val isReady: Boolean
        get() = imu.isGyroCalibrated

    private fun CoroutineScope.orientation(ticker: ReceiveChannel<Unit>) = produce<Orientation> {
        while (isActive) {
            val orientation = imu.getAngularOrientation(
                AxesReference.INTRINSIC,
                AxesOrder.ZYX,
                AngleUnit.DEGREES
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
        val ticker = ticker(
            delay = 10,
            mode = TickerMode.FIXED_DELAY
        )
        val orientation = coroutineScope.orientation(ticker)
        return coroutineScope.heading(orientation)
    }

    /**
     * Configures a [RobotIMULocalizer].
     *
     * @param coroutineScope The [CoroutineScope] that the channels should run on.
     */
    class Configuration(val coroutineScope: CoroutineScope) : RobotFeatureConfiguration {

        /**
         * The name of the imu as used in the configuration file.
         */
        var imuName: String = "imu"
    }

    companion object FeatureInstaller : RobotFeatureInstaller<Configuration, RobotIMULocalizer> {

        override fun install(robot: Robot, configure: Configuration.() -> Unit): RobotIMULocalizer {
            val configuration = Configuration(robot as CoroutineScope).apply(configure)
            val imu = robot.hardwareMap.get(BNO055IMU::class.java, configuration.imuName)
                .apply { initialize(BNO055IMU.Parameters()) }
            return RobotIMULocalizer(imu, configuration.coroutineScope)
        }

    }

}