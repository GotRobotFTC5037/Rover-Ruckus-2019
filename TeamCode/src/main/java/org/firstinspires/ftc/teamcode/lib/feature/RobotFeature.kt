package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@DslMarker
annotation class RobotFeatureMarker

@RobotFeatureMarker
interface RobotFeature

@RobotFeatureMarker
interface RobotFeatureConfiguration

@RobotFeatureMarker
interface RobotFeatureInstaller<out TConfiguration : RobotFeatureConfiguration, TObject : RobotFeature> {
    fun install(robot: Robot, configure: TConfiguration.() -> Unit): TObject
}

class RobotFeatureSet {

    private val set = mutableSetOf<RobotFeature>()

    fun add(feature: RobotFeature) {
        if (feature !in this) {
            set.add(feature)
        }
    }

    operator fun plus(feature: RobotFeature) {
        add(feature)
    }

    operator fun contains(featureClass: KClass<out RobotFeature>): Boolean {
        set.forEach { if (it::class.isSubclassOf(featureClass)) return true }
        return false
    }

    operator fun contains(feature: RobotFeature): Boolean = contains(feature::class)

    @Suppress("UNCHECKED_CAST")
    operator fun <TFeature : RobotFeature> get(feature: KClass<TFeature>): TFeature? =
        set.singleOrNull { it::class.isSubclassOf(feature) } as? TFeature

}