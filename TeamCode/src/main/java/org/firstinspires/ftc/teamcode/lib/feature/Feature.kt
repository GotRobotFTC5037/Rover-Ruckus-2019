package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.teamcode.lib.robot.RobotFeatureInstallContext
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class Feature

interface FeatureConfiguration

abstract class FeatureInstaller<TFeature : Feature, TConfiguration : FeatureConfiguration> {

    abstract val name: String

    abstract suspend fun install(
        context: RobotFeatureInstallContext,
        featureSet: FeatureSet,
        configure: TConfiguration.() -> Unit
    ): TFeature

}

interface FeatureKey<T : Any>

abstract class KeyedFeatureInstaller<F : Feature, C : FeatureConfiguration> :
    FeatureInstaller<F, C>(), FeatureKey<F>

interface FeatureSet : Iterable<Feature> {

    operator fun <F : Feature> contains(key: FeatureKey<F>): Boolean

    operator fun <F : Feature> contains(featureClass: KClass<F>): Boolean

    operator fun <F : Feature> get(key: FeatureKey<F>): F

    operator fun <F : Any> get(key: FeatureKey<*>): F

}

class MutableFeatureSet : FeatureSet {

    private val features: MutableMap<FeatureKey<*>, Feature> = mutableMapOf()

    operator fun <F : Feature> set(key: FeatureKey<F>, feature: F) {
        if (key !in this) {
            features[key] = feature
        }
    }

    override fun <F : Feature> contains(key: FeatureKey<F>): Boolean = key in features

    override fun <F : Feature> contains(featureClass: KClass<F>): Boolean =
        features.toList().any { it.second::class.isSubclassOf(featureClass) }


    @Suppress("UNCHECKED_CAST")
    override fun <F : Feature> get(key: FeatureKey<F>): F = features[key] as F

    @Suppress("UNCHECKED_CAST")
    override fun <F : Any> get(key: FeatureKey<*>): F = features[key] as F

    override fun iterator(): Iterator<Feature> = object : Iterator<Feature> {
        override fun hasNext(): Boolean = features.iterator().hasNext()
        override fun next(): Feature = features.iterator().next().component2()
    }

    fun toFeatureSet(): FeatureSet = this

}