package org.firstinspires.ftc.teamcode.lib

@DslMarker
annotation class RobotFeatureMarker

class RobotFeatureKey<out T : Any>(val name: String)

interface RobotFeatureDescriptor<T : Any> {

    val key: RobotFeatureKey<T>

}

@RobotFeatureMarker
interface RobotFeature<out TConfiguration : RobotFeatureConfiguration, TFeature : Any> :
    RobotFeatureDescriptor<TFeature> {
    fun install(robot: Robot, configure: TConfiguration.() -> Unit): TFeature
}

class RobotFeatureSet {

    private val map = mutableMapOf<RobotFeatureKey<*>, Any>()

    fun <T : Any> add(feature: RobotFeature<*, T>, instance: T) {
        map[feature.key] = instance
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrNull(feature: RobotFeatureDescriptor<T>): T? {
        return map[feature.key] as? T
    }

}

@RobotFeatureMarker
interface RobotFeatureConfiguration
