@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.Vuforia
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.hardwareMap
import org.firstinspires.ftc.teamcode.lib.util.objectDetector
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

    val goldPosition: ReceiveChannel<GoldPosition>

    class Configuration : TFObjectDetector.Parameters(), FeatureConfiguration

    fun shutdown()

    companion object Installer : FeatureInstaller<Configuration, CargoDetector> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): CargoDetector {
            val configuration = Configuration().apply(configure)
            val context = robot.hardwareMap.appContext
            val viewId = context.resources.getIdentifier(VIEW_ID, "id", context.packageName)
            val parameters: TFObjectDetector.Parameters =
                configuration.apply { tfodMonitorViewIdParent = viewId }
            val vuforia = robot[Vuforia]
            val objectDetector = objectDetector(parameters, vuforia.localizer).apply {
                loadModelFromAsset(TFOD_MODEL_ASSET, GOLD_MINERAL, SILVER_MINERAL)
            }
            return CargoDetectorImpl(objectDetector, robot.coroutineContext)
        }
    }
}

class CargoDetectorImpl(
    private val objectDetector: TFObjectDetector,
    override val coroutineContext: CoroutineContext
) : CargoDetector, CoroutineScope {

    override val goldPosition: ReceiveChannel<GoldPosition> =
        produceGoldPosition(objectDetector)

    private fun CoroutineScope.produceGoldPosition(
        objectDetector: TFObjectDetector
    ) = produce(capacity = Channel.CONFLATED) {
        objectDetector.activate()
        invokeOnClose {
            shutdown()
        }
        while (true) {
            val recognitions = objectDetector.updatedRecognitions
            if (recognitions != null) {
                val gold = recognitions.filter { it.label == GOLD_MINERAL }
                val silver = recognitions.filter { it.label == SILVER_MINERAL }
                val position = if (gold.count() == 1 && silver.count() == 2) {
                    when {
                        silver.none { it.left > gold.first().left } -> GoldPosition.RIGHT
                        silver.none { it.right < gold.first().right } -> GoldPosition.LEFT
                        else -> GoldPosition.CENTER
                    }
                } else {
                    GoldPosition.UNKNOWN
                }
                send(position)
                yield()
            } else {
                yield()
            }
        }
    }

    override fun shutdown() {
        GlobalScope.launch(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {
            objectDetector.shutdown()
        }
    }

}
