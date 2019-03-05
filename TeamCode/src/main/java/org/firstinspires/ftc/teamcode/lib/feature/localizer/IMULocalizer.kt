package org.firstinspires.ftc.teamcode.lib.feature.localizer

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.yield
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.KeyedFeatureInstaller

class IMULocalizer(
    private val imu: BNO055IMU
) : Feature(), HeadingLocalizer {

    override val currentHeading: Double
        get() = imu.getAngularOrientation(
            AxesReference.INTRINSIC,
            AxesOrder.ZYX,
            AngleUnit.DEGREES
        ).firstAngle.toDouble()

    companion object Installer : KeyedFeatureInstaller<IMULocalizer, Configuration>() {

        override val featureName: String = "IMU Localizer"

        override suspend fun install(
            hardwareMap: HardwareMap,
            configure: Configuration.() -> Unit
        ): IMULocalizer {
            val configuration = Configuration().apply(configure)
            val imu = hardwareMap.get(BNO055IMU::class.java, configuration.imuName)
            return IMULocalizer(imu)
        }

        private suspend fun BNO055IMU.suspendUntilGyroCalibration() {
            while (!isGyroCalibrated) {
                yield()
            }
        }

    }

    class Configuration : FeatureConfiguration {
        var imuName: String = "imu"
    }

}

