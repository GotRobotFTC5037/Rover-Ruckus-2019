package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.robot.Robot

@DslMarker
annotation class RobotFeatureMarker

interface Feature

@RobotFeatureMarker
interface FeatureConfiguration

interface FeatureKey<out F : Feature>

fun <F : Feature> featureKey() = object : FeatureKey<F> {}

interface FeatureInstaller<C : FeatureConfiguration, F : Feature> : FeatureKey<F> {
    val name: String get() = "feature"
    fun install(robot: Robot, configure: C.() -> Unit): F
}

class InvalidFeatureConfigurationException(message: String): RuntimeException(message)