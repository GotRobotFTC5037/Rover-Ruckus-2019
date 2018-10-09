package us.gotrobot.grbase

import com.qualcomm.hardware.bosch.BNO055IMU
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.isActive
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference
import org.firstinspires.ftc.robotcore.external.navigation.Orientation

data class RobotPosition(
    val x: Double,
    val y: Double
)

interface RobotHeadingLocalizer {
    val headingChannel: ReceiveChannel<Double>

    companion object Descriptor : RobotFeatureDescriptor<RobotHeadingLocalizer> {
        override val key = RobotFeatureKey<RobotHeadingLocalizer>("RobotHeadingLocalizer")
    }
}

class IMULocalizer(
    private val imu: BNO055IMU,
    private val coroutineScope: CoroutineScope
): RobotHeadingLocalizer {

    private fun CoroutineScope.orientation() = produce<Orientation> {
        while(isActive) {
            val orientation = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES)
            offer(orientation)
            delay(10)
        }
    }

    private fun CoroutineScope.heading(orientation: ReceiveChannel<Orientation>) = produce<Double> {
        while(isActive) {
            val orientation = orientation.receive()
            offer(orientation.firstAngle.toDouble())
        }
    }

    override val headingChannel: ReceiveChannel<Double> by lazy {
        val orientation = coroutineScope.orientation()
        coroutineScope.heading(orientation)
    }

    class Configuration(val coroutineScope: CoroutineScope) : RobotFeatureConfiguration {
        var imuName = "imu"
    }

    companion object Feature : RobotFeature<Configuration, IMULocalizer> {

        override val key = RobotFeatureKey<IMULocalizer>("IMULocalizer")

        override fun install(robot: Robot, configure: Configuration.() -> Unit): IMULocalizer {
            val configuration = Configuration(robot).apply(configure)
            val imu = robot.hardwareMap.get(BNO055IMU::class.java, configuration.imuName)
                .apply { initialize(BNO055IMU.Parameters()) }
            return IMULocalizer(imu, configuration.coroutineScope)
        }

    }

}