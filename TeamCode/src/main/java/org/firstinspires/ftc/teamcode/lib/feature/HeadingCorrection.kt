package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.PipelineContext
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot

class HeadingCorrection(
    localizer: IMULocalizer,
    private val targetHeading: TargetHeading,
    private val coefficient: Double
) : Feature {

    var enabled = true

    private val headingChannel = localizer.newHeadingChannel()

    suspend fun interceptor(context: PipelineContext<TankDriveTrain.MotorPowers, TankDriveTrain>) {
        if (enabled) {
            val currentPowers = context.subject
            val targetHeading = targetHeading.targetHeading
            val currentHeading = headingChannel.receive()
            val error = currentHeading - targetHeading
            val correction = coefficient * error
            val newPowerPowers = TankDriveTrain.MotorPowers(
                currentPowers.left - correction,
                currentPowers.right + correction
            )
            context.proceedWith(newPowerPowers)
        } else {
            context.proceed()
        }
    }

    fun actionInterceptor(context: PipelineContext<Action, Robot>) {
        val subject = context.subject
        if (subject is MoveAction) {
            enabled = when(subject.context.type) {
                is Drive -> true
                is TurnTo -> false
                else -> true
            }
        }
    }

    companion object Installer : FeatureInstaller<Configuration, HeadingCorrection> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): HeadingCorrection {
            val localizer = robot[IMULocalizer]
            val targetHeading = robot[TargetHeading]
            val configuration = Configuration().apply(configure)
            val headingCorrection = HeadingCorrection(localizer, targetHeading, configuration.coefficient)
            robot[TankDriveTrain].powerPipeline.intercept {
                headingCorrection.interceptor(this)
            }
            robot.actionPipeline.intercept {
                headingCorrection.actionInterceptor(this)
            }
            return headingCorrection
        }
    }

    class Configuration : FeatureConfiguration{
        var coefficient = 0.0
    }
}
