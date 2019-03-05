package org.firstinspires.ftc.teamcode.lib.robot

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.feature.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

private class RobotImpl(
    private val telemetry: Telemetry,
    private val hardwareMap: HardwareMap,
    private val parentContext: CoroutineContext
) : Robot, RobotFeatureInstaller, CoroutineScope {

    private val job: Job = Job(parentContext[Job])

    override val coroutineContext: CoroutineContext
        get() = parentContext + CoroutineName("Robot") + job

    private val features: MutableFeatureSet = MutableFeatureSet()

    override suspend fun <F : Feature, C : FeatureConfiguration> install(
        installer: FeatureInstaller<F, C>,
        key: FeatureKey<F>,
        configure: C.() -> Unit
    ) {
        if (key !in features) {
            telemetry.log().add("[Robot] Installing ${installer.featureName}")
            val feature = installer.install(hardwareMap, configure)
            features[key] = feature
        } else {
            throw InvalidInstallKeyException()
        }
    }

    override suspend fun perform(action: Action) = withContext(coroutineContext) {
        action.run(features)
    }
}

suspend fun OpMode.robot(configure: suspend RobotFeatureInstaller.() -> Unit = {}): Robot {
    val robot = RobotImpl(telemetry, hardwareMap, coroutineContext)
    robot.configure()
    return robot
}

class MissingFeatureException : RuntimeException()

class InvalidInstallKeyException : RuntimeException()