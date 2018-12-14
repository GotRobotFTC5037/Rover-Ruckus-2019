package org.firstinspires.ftc.teamcode.lib.feature.localizer

import com.qualcomm.hardware.bosch.BNO055IMU
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
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
    private val pollRate: Long,
    override val coroutineContext: CoroutineContext
) : RobotHeadingLocalizer, CoroutineScope {

    private fun CoroutineScope.broadcastHeading() =
        broadcast(capacity = Channel.CONFLATED) {
            while (true) {
                val orientation = imu.getAngularOrientation(
                    AxesReference.INTRINSIC,
                    AxesOrder.YZX,
                    AngleUnit.DEGREES
                )
                val heading = orientation.firstAngle.toDouble()
                send(heading)
                yield()
            }
        }

    fun newHeadingChannel() = broadcastHeading().openSubscription()

    class Configuration : FeatureConfiguration {
        var imuName: String = "imu"
        var pollRate: Long = 10
    }

    companion object Installer :
        FeatureInstaller<Configuration, IMULocalizer> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): IMULocalizer {
            val configuration = Configuration().apply(configure)
            val imu = robot.hardwareMap.get(BNO055IMU::class.java, configuration.imuName)
            imu.initialize(BNO055IMU.Parameters())
            return IMULocalizer(imu, configuration.pollRate, robot.coroutineContext)
        }
    }

}