package us.gotrobot.grbase

@DslMarker
annotation class RobotFeatureMarker

interface RobotFeature<out TConfiguration : RobotFeatureConfiguration, TFeature : Any> : RobotFeatureDescriptor<TFeature> {
    fun install(robot: Robot, configure: TConfiguration.() -> Unit): TFeature
}

interface RobotFeatureDescriptor<TFeature : Any> {
    val key: RobotFeatureKey<TFeature>
}

class RobotFeatureKey<T : Any>(val name: String)

@RobotFeatureMarker
interface RobotFeatureConfiguration
