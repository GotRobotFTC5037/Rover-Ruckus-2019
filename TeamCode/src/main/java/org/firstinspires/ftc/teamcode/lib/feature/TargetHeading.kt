package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.feature.localizer.HeadingLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.RobotFeatureInstallContext
import kotlin.math.abs

class TargetHeading(
    private val headingLocalizer: HeadingLocalizer,
    initialTargetHeading: Double = 0.0
) : Feature() {

    var targetHeading: Double = initialTargetHeading
        set(value) {
            field = properHeading(value)
        }

    suspend fun deltaFromTarget(): Double {
        val currentHeading = headingLocalizer.heading()
        val delta = targetHeading - currentHeading
        return if (abs(delta) <= abs(delta - 360)) delta
        else delta - 360
    }

    suspend fun deltaFromHeading(heading: Double): Double {
        val currentHeading = headingLocalizer.heading()
        val delta = properHeading(heading) - currentHeading
        return if (abs(delta) <= abs(delta - 360)) delta
        else delta - 360
    }

    suspend fun resetToCurrentHeading() {
        targetHeading = headingLocalizer.heading()
    }

    companion object Installer : KeyedFeatureInstaller<TargetHeading, Configuration>() {

        override val name: String = "Target Heading"

        override suspend fun install(
            context: RobotFeatureInstallContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): TargetHeading {
            val config = Configuration().apply(configure)
            val headingLocalizer: HeadingLocalizer = featureSet[config.headingLocalizerKey]
            return TargetHeading(headingLocalizer)
        }
    }

    class Configuration : FeatureConfiguration {
        lateinit var headingLocalizerKey: FeatureKey<IMULocalizer>
    }

}

tailrec fun properHeading(heading: Double): Double = when {
    heading >= 180.0 -> properHeading(heading - 360)
    heading < -180.0 -> properHeading(heading + 360)
    else -> heading
}