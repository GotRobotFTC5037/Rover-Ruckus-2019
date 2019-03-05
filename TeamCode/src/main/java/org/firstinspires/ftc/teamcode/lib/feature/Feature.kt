package org.firstinspires.ftc.teamcode.lib.feature

import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.lib.robot.MissingFeatureException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class Feature {

    internal lateinit var featureLogging: FeatureLogging

}

fun Feature.log(message: String) = featureLogging.log(message)

interface FeatureConfiguration

abstract class FeatureInstaller<TFeature : Feature, TConfiguration : FeatureConfiguration> {

    abstract val featureName: String

    abstract suspend fun install(
        hardwareMap: HardwareMap,
        configure: TConfiguration.() -> Unit
    ): TFeature

}

interface FeatureKey<T : Feature>

abstract class KeyedFeatureInstaller<F : Feature, C : FeatureConfiguration> :
    FeatureInstaller<F, C>(), FeatureKey<F>

interface FeatureSet : Iterable<Feature> {

    operator fun <F : Feature> contains(featureKey: FeatureKey<F>): Boolean

    operator fun <F : Feature> contains(featureClass: KClass<F>): Boolean

    operator fun <F : Feature> get(featureKey: FeatureKey<F>): F

    operator fun <F : Feature> get(featureClass: KClass<F>): F

}

class MutableFeatureSet : FeatureSet {

    private val features: MutableMap<FeatureKey<*>, Feature> = mutableMapOf()

    operator fun <F : Feature> set(key: FeatureKey<F>, feature: F) {
        if (key !in this) {
            features[key] = feature
        }
    }

    override fun <F : Feature> contains(featureKey: FeatureKey<F>): Boolean =
        featureKey in features

    override fun <F : Feature> contains(featureClass: KClass<F>): Boolean =
        features.toList().any { it.second::class.isSubclassOf(featureClass) }


    @Suppress("UNCHECKED_CAST")
    override fun <F : Feature> get(featureKey: FeatureKey<F>): F = features[featureKey] as F

    @Suppress("UNCHECKED_CAST")
    override fun <F : Feature> get(featureClass: KClass<F>): F =
        features.toList().singleOrNull { it.second::class.isSubclassOf(featureClass) }
                as? F ?: throw MissingFeatureException()


    override fun iterator(): Iterator<Feature> = object : Iterator<Feature> {
        override fun hasNext(): Boolean = features.iterator().hasNext()
        override fun next(): Feature = features.iterator().next().component2()
    }

    fun toFeatureSet(): FeatureSet = this

}