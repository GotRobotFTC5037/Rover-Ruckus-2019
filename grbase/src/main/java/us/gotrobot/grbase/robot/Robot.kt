package us.gotrobot.grbase.robot

import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.CoroutineScope
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.gotrobot.grbase.action.Action
import us.gotrobot.grbase.feature.*
import us.gotrobot.grbase.pipeline.Pipeline

interface Robot {
    val features: FeatureSet
    suspend fun perform(action: Action)
}

interface FeatureInstallContext {

    val features: FeatureSet

    val hardwareMap: HardwareMap

    val telemetry: Telemetry

    val gamepads: Pair<Gamepad, Gamepad>

    val coroutineScope: CoroutineScope

    val actionPipeline: Pipeline<Action, FeatureInstallContext>

    suspend fun <F : Feature, C : FeatureConfiguration> install(
        installer: FeatureInstaller<F, C>,
        key: FeatureKey<F>,
        configure: C.() -> Unit
    ): F

}

suspend fun <F : Feature, C : FeatureConfiguration> FeatureInstallContext.install(
    installer: KeyedFeatureInstaller<F, C>,
    configure: C.() -> Unit
): F = install(installer, installer, configure)
