package org.firstinspires.ftc.teamcode.lib.robot

import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.feature.*
import kotlin.reflect.KClass

@RobotFeatureMarker
interface Robot {

    fun <TConfiguration : FeatureConfiguration, TFeature : Feature> install(
        feature: FeatureInstaller<TConfiguration, TFeature>,
        configuration: TConfiguration.() -> Unit = {}
    )

    operator fun <F : Feature> get(key: FeatureKey<F>): F?

    operator fun <F : Feature> get(featureClass: KClass<F>): F?

    fun start()

    fun perform(action: Action)

}
