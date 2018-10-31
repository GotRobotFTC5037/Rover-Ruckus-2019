@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active.features

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.broadcast
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.objectDetection.Vuforia
import org.firstinspires.ftc.teamcode.lib.objectDetector
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

private const val TFOD_MODEL_ASSET = "RoverRuckus.tflite"
private const val GOLD_MINERAL = "Gold Mineral"
private const val SILVER_MINERAL = "Silver Mineral"
private const val VIEW_ID = "tfodMonitorViewId"

enum class GoldPosition {
    LEFT, CENTER, RIGHT, UNKNOWN
}

interface CargoDetector : Feature {

    val goldPosition: BroadcastChannel<GoldPosition>

    companion object Installer : FeatureInstaller<Nothing, CargoDetector> {
        override fun install(
            robot: Robot,
            coroutineContext: CoroutineContext,
            configure: Nothing.() -> Unit
        ): CargoDetector {
            val context = robot.hardwareMap.appContext
            val viewId = context.resources.getIdentifier(VIEW_ID, "id", context.packageName)
            val parameters = TFObjectDetector.Parameters(viewId)
            val vuforia = robot[Vuforia]
            val objectDetector = objectDetector(parameters, vuforia.localizer).apply {
                loadModelFromAsset(TFOD_MODEL_ASSET, GOLD_MINERAL, SILVER_MINERAL)
                activate()
            }
            return CargoDetectorImpl(objectDetector)
        }
    }
}

class CargoDetectorImpl(objectDetector: TFObjectDetector) : CargoDetector, CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Executors.newSingleThreadExecutor().asCoroutineDispatcher() + job

    override val goldPosition: BroadcastChannel<GoldPosition> =
        broadcastGoldPosition(objectDetector)

    private fun CoroutineScope.broadcastGoldPosition(
        objectDetector: TFObjectDetector
    ) = broadcast(capacity = Channel.CONFLATED) {
        while (true) {
            val recognitions = objectDetector.updatedRecognitions ?: continue
            val gold = recognitions.filter { it.label == GOLD_MINERAL }
            val silver = recognitions.filter { it.label == SILVER_MINERAL }
            val position = if (gold.count() == 1 && silver.count() == 2) {
                when {
                    silver.none { it.left < gold.first().left } -> GoldPosition.RIGHT
                    silver.none { it.right > gold.first().right } -> GoldPosition.LEFT
                    else -> GoldPosition.CENTER
                }
            } else {
                GoldPosition.UNKNOWN
            }
            send(position)
        }
    }

}