package org.firstinspires.ftc.teamcode.lib.robot

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.lib.action.Action
import org.firstinspires.ftc.teamcode.lib.feature.*
import org.firstinspires.ftc.teamcode.lib.pipeline.Pipeline
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

private class RobotImpl(
    private val telemetry: Telemetry,
    override val hardwareMap: HardwareMap,
    private val parentContext: CoroutineContext
) : Robot, RobotFeatureInstallContext, CoroutineScope {

    private val job: Job = Job(parentContext[Job])

    override val coroutineContext: CoroutineContext
        get() = parentContext + CoroutineName("Robot") + job

    private val features: MutableFeatureSet = MutableFeatureSet()

    override val actionPipeline = Pipeline<Action, RobotFeatureInstallContext>()

    override suspend fun <F : Feature, C : FeatureConfiguration> install(
        installer: FeatureInstaller<F, C>,
        key: FeatureKey<F>,
        configure: C.() -> Unit
    ) {
        if (key !in features) {
            telemetry.log().add("[Robot] Installing ${installer.name}")
            val feature = installer.install(this, features, configure)
            features[key] = feature
        } else {
            throw InvalidInstallKeyException()
        }
    }

    override suspend fun perform(action: Action) = withContext(coroutineContext) {
        val modifiedAction = actionPipeline.execute(action, this@RobotImpl)
        modifiedAction.run(features)
    }
}

suspend fun OpMode.robot(configure: suspend RobotFeatureInstallContext.() -> Unit = {}): Robot {
    val robot = RobotImpl(telemetry, hardwareMap, coroutineContext)
    robot.configure()
    return robot
}

class MissingFeatureException : RuntimeException()

class InvalidInstallKeyException : RuntimeException()