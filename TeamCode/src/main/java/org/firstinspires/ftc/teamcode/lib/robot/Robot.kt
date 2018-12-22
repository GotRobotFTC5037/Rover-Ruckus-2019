package org.firstinspires.ftc.teamcode.lib.robot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.CoroutineScope
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.action.ActionPipeline
import org.firstinspires.ftc.teamcode.lib.action.ActionScope
import org.firstinspires.ftc.teamcode.lib.action.action
import org.firstinspires.ftc.teamcode.lib.feature.*
import kotlin.reflect.KClass

@RobotFeatureMarker
interface Robot : CoroutineScope {

    val linearOpMode: LinearOpMode

    val actionPipeline: ActionPipeline

    val opmodeScope: CoroutineScope

    fun <TConfiguration : FeatureConfiguration, TFeature : Feature> install(
        feature: FeatureInstaller<TConfiguration, TFeature>,
        key: FeatureKey<TFeature>,
        configuration: TConfiguration.() -> Unit = {}
    )

    operator fun contains(key: FeatureKey<*>): Boolean

    operator fun contains(featureClass: KClass<Feature>): Boolean

    operator fun <F : Feature> get(key: FeatureKey<F>): F

    operator fun <F : Feature> get(featureClass: KClass<F>): F

    suspend fun perform(action: Action)

    fun performBlocking(action: Action)

}

val Robot.telemetry: Telemetry get() = this.linearOpMode.telemetry
val Robot.hardwareMap: HardwareMap get() = this.linearOpMode.hardwareMap

fun <TConfiguration : FeatureConfiguration, TFeature : Feature> Robot.install(
    feature: FeatureInstaller<TConfiguration, TFeature>,
    configuration: TConfiguration.() -> Unit
) {
    install(feature, feature, configuration)
}

suspend fun Robot.perform(block: suspend ActionScope.() -> Unit) {
    val action = action(block)
    perform(action)
}

class MissingRobotFeatureException(message: String? = null) : RuntimeException(message)
