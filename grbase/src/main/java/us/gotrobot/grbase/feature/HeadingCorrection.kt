package us.gotrobot.grbase.feature

import us.gotrobot.grbase.feature.drivetrain.InterceptableDriveTrain
import us.gotrobot.grbase.feature.drivetrain.MecanumDriveTrain
import us.gotrobot.grbase.feature.localizer.IMULocalizer
import us.gotrobot.grbase.robot.FeatureInstallContext

class HeadingCorrection : Feature() {

    var enabled: Boolean = true

    companion object Installer : KeyedFeatureInstaller<HeadingCorrection, Configuration>() {

        override val name: String = "Heading Correction"

        override suspend fun install(
            context: FeatureInstallContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): HeadingCorrection {
            val driveTrain: InterceptableDriveTrain<*> = featureSet[MecanumDriveTrain]
            val targetHeading = featureSet[TargetHeading]
            val localizer = featureSet[IMULocalizer]

            val configuration = Configuration().apply(configure)
            val coefficient = configuration.coefficient
            val maxValue = configuration.maxValue

            val headingCorrection = HeadingCorrection()

            val headingChannel = localizer.headingChannel()

            driveTrain.powerPipeline.intercept {
                if (headingCorrection.enabled) {
                    val current = headingChannel.receive()
                    val target = targetHeading.targetHeading
                    val adjustment = ((target - current) * coefficient).coerceAtMost(maxValue)
                    subject.adjustHeadingPower(adjustment)
                }
                proceed()
            }
            return headingCorrection
        }

    }

    class Configuration : FeatureConfiguration {
        var coefficient: Double = 0.0
        var maxValue: Double = 0.0
    }
}
