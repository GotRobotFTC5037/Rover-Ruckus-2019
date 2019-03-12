package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.DriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.DriveTrainMotorPowers
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.InterceptableDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.MecanumDriveTrain
import org.firstinspires.ftc.teamcode.lib.pipeline.PipelineContext
import org.firstinspires.ftc.teamcode.lib.robot.RobotFeatureInstallContext

class HeadingCorrection(
    private val targetHeading: TargetHeading,
    private val coefficient: Double
) : Feature() {

    var enabled: Boolean = true

    suspend fun interceptor(context: PipelineContext<out DriveTrainMotorPowers, DriveTrain>) {
        if (enabled) {
            context.subject.adjustHeadingPower(targetHeading.deltaFromTarget() * coefficient)
        }
        context.proceed()
    }

    companion object Installer : KeyedFeatureInstaller<HeadingCorrection, Configuration>() {

        override val name: String = "Heading Correction"

        override suspend fun install(
            context: RobotFeatureInstallContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): HeadingCorrection {
            val driveTrain: InterceptableDriveTrain<*> = featureSet[MecanumDriveTrain]
            val targetHeading = featureSet[TargetHeading]
            val configuration = Configuration().apply(configure)
            val headingCorrection = HeadingCorrection(targetHeading, configuration.coefficient)
            driveTrain.powerPipeline.intercept {
                headingCorrection.interceptor(this)
            }
            return headingCorrection
        }

    }

    class Configuration : FeatureConfiguration {
        var coefficient: Double = 0.0
    }
}