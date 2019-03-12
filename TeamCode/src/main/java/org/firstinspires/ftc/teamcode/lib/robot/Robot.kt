package org.firstinspires.ftc.teamcode.lib.robot

import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.feature.*
import org.firstinspires.ftc.teamcode.lib.pipeline.Pipeline

interface Robot {
    suspend fun perform(action: Action)
}

interface RobotFeatureInstallContext {

    val hardwareMap: HardwareMap

    val actionPipeline: Pipeline<Action, RobotFeatureInstallContext>

    suspend fun <F : Feature, C : FeatureConfiguration> install(
        installer: FeatureInstaller<F, C>,
        key: FeatureKey<F>,
        configure: C.() -> Unit
    )

}

suspend fun <F : Feature, C : FeatureConfiguration> RobotFeatureInstallContext.install(
    installer: KeyedFeatureInstaller<F, C>,
    configure: C.() -> Unit
) {
    install(installer, installer, configure)
}
