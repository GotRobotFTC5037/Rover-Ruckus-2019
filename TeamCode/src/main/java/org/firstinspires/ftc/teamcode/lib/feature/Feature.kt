package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.robot.Robot

@DslMarker
annotation class RobotFeatureMarker

interface Feature

@RobotFeatureMarker
interface FeatureConfiguration

interface FeatureKey<out F : Feature>

interface FeatureInstaller<C : FeatureConfiguration, F : Feature> : FeatureKey<F> {
    fun install(robot: Robot, configure: C.() -> Unit): F
}
