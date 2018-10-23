package org.firstinspires.ftc.teamcode.lib.feature

import com.qualcomm.robotcore.hardware.HardwareMap
import kotlin.coroutines.CoroutineContext

@DslMarker
annotation class RobotFeatureMarker

@RobotFeatureMarker
interface Feature

@RobotFeatureMarker
interface FeatureConfiguration

interface FeatureKey<out F : Feature>

interface FeatureInstaller<C : FeatureConfiguration, F : Feature> : FeatureKey<F> {
    fun install(
        hardwareMap: HardwareMap,
        coroutineContext: CoroutineContext,
        configure: C.() -> Unit
    ): F
}
