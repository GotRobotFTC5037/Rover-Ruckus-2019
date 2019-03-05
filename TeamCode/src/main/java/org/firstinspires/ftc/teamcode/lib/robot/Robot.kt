package org.firstinspires.ftc.teamcode.lib.robot

import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.feature.*
import kotlin.reflect.KClass

interface Robot {
    suspend fun perform(action: Action)
}

interface RobotFeatureInstaller {
    suspend fun <F : Feature, C : FeatureConfiguration> install(
        installer: FeatureInstaller<F, C>,
        key: FeatureKey<F>,
        configure: C.() -> Unit
    )
}

suspend fun <F : Feature, C : FeatureConfiguration> RobotFeatureInstaller.install(
    installer: KeyedFeatureInstaller<F, C>,
    configure: C.() -> Unit
) {
    install(installer, installer, configure)
}

