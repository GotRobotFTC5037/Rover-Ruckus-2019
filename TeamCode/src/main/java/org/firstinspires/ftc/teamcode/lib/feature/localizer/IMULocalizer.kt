package org.firstinspires.ftc.teamcode.lib.feature.localizer

import com.qualcomm.hardware.bosch.BNO055IMU
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.TickerMode
import kotlinx.coroutines.channels.ticker
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference
import org.firstinspires.ftc.robotcore.external.navigation.Orientation
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureSet
import org.firstinspires.ftc.teamcode.lib.feature.KeyedFeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.RobotFeatureInstallContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class IMULocalizer(
    private val imu: BNO055IMU,
    private val parentContext: CoroutineContext
) : Feature(), OrientationLocalizer, CoroutineScope {

    private val job: Job = Job(parentContext[Job])

    override val coroutineContext: CoroutineContext
        get() = parentContext + CoroutineName("IMU Localizer") + job

    private val orientationChannel: Channel<Orientation> = Channel(Channel.CONFLATED)
    private val headingChannel: Channel<Double> = Channel(Channel.CONFLATED)

    private fun CoroutineScope.startUpdatingOrientation(ticker: ReceiveChannel<Unit>) = launch {
        while (isActive) {
            ticker.receive()
            val orientation = imu.getAngularOrientation(IMUAngularOrientationOptions())
            orientationChannel.send(orientation)
            headingChannel.send(orientation.firstAngle.toDouble())
        }
    }

    override suspend fun orientation() = orientationChannel.receive()

    override suspend fun heading() = headingChannel.receive()

    fun rawHeading() =
        imu.getAngularOrientation(IMUAngularOrientationOptions()).firstAngle.toDouble()

    companion object Installer : KeyedFeatureInstaller<IMULocalizer, Configuration>() {

        override val name: String = "IMU Localizer"

        override suspend fun install(
            context: RobotFeatureInstallContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): IMULocalizer {
            val configuration = Configuration().apply(configure)
            val imu = context.hardwareMap.get(BNO055IMU::class.java, configuration.imuName)
            val parameters = BNO055IMU.Parameters().apply {
                angleUnit = BNO055IMU.AngleUnit.DEGREES
            }
            imu.initialize(parameters)
            return IMULocalizer(imu, coroutineContext).apply {
                @Suppress("EXPERIMENTAL_API_USAGE")
                startUpdatingOrientation(
                    ticker(
                        10,
                        0,
                        this.coroutineContext,
                        TickerMode.FIXED_PERIOD
                    )
                )
            }
        }

    }

    class Configuration : FeatureConfiguration {
        var imuName: String = "imu"
    }

}

data class IMUAngularOrientationOptions(
    val reference: AxesReference = AxesReference.INTRINSIC,
    val order: AxesOrder = AxesOrder.ZYX,
    val unit: AngleUnit = AngleUnit.DEGREES
)

fun BNO055IMU.getAngularOrientation(
    options: IMUAngularOrientationOptions
): Orientation {
    val (ref, order, unit) = options
    return getAngularOrientation(ref, order, unit)
}

suspend fun BNO055IMU.suspendUntilGyroCalibration() {
    while (!isGyroCalibrated) {
        yield()
    }
}

