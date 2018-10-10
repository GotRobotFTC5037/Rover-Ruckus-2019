package org.firstinspires.ftc.teamcode.lib

@DslMarker
annotation class RobotFeatureMarker

@RobotFeatureMarker
interface RobotFeature<out TConfiguration : RobotFeatureConfiguration, TFeature : Any> : RobotFeatureDescriptor<TFeature> {
    fun install(robot: Robot, configure: TConfiguration.() -> Unit): TFeature
}

interface RobotFeatureDescriptor<TFeature : Any> {
    val key: RobotFeatureKey<TFeature>
}

class RobotFeatureKey<out T : Any>(val name: String)

@RobotFeatureMarker
interface RobotFeatureConfiguration
