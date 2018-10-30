package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.CoroutineContext

@DslMarker
annotation class RobotFeatureMarker

@RobotFeatureMarker
interface Feature

@RobotFeatureMarker
interface FeatureConfiguration

interface FeatureKey<out F : Feature>

interface FeatureInstaller<C : FeatureConfiguration, F : Feature> : FeatureKey<F> {
    fun install(robot: Robot, coroutineContext: CoroutineContext, configure: C.() -> Unit): F
}
