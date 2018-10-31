package org.firstinspires.ftc.teamcode.lib.robot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.feature.*
import kotlin.reflect.KClass

@RobotFeatureMarker
interface Robot {

    val linearOpMode: LinearOpMode

    val hardwareMap: HardwareMap
        get() = linearOpMode.hardwareMap

    fun <TConfiguration : FeatureConfiguration, TFeature : Feature> install(
        feature: FeatureInstaller<TConfiguration, TFeature>,
        configuration: TConfiguration.() -> Unit = {}
    )

    operator fun contains(key: FeatureKey<*>): Boolean

    operator fun contains(featureClass: KClass<Feature>): Boolean

    operator fun <F : Feature> get(key: FeatureKey<F>): F

    operator fun <F : Feature> get(featureClass: KClass<F>): F

    fun perform(action: Action)

}

class MissingRobotFeatureException(message: String? = null) : RuntimeException(message)
