@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.firstinspires.ftc.teamcode.active.features

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.isActive
import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.objectDetection.Vuforia
import org.firstinspires.ftc.teamcode.lib.robot.MissingRobotFeatureException
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

private const val TFOD_MODEL_ASSET = "RoverRuckus.tflite"
private const val LABEL_GOLD_MINERAL = "Gold Mineral"
private const val LABEL_SILVER_MINERAL = "Silver Mineral"

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
            val tfodMonitorViewId = robot.hardwareMap.appContext.resources.getIdentifier(
                "tfodMonitorViewId", "id", robot.hardwareMap.appContext.packageName
            )
            val tfodParameters = TFObjectDetector.Parameters(tfodMonitorViewId)
            val vuforia = robot[Vuforia]
                ?: throw MissingRobotFeatureException("CargoDetector requires Vuforia to be installed.")
            val objectDetector =
                ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia.localizer)
            objectDetector.loadModelFromAsset(
                TFOD_MODEL_ASSET,
                LABEL_GOLD_MINERAL,
                LABEL_SILVER_MINERAL
            )
            return CargoDetectorImpl(objectDetector)
        }
    }
}

class CargoDetectorImpl(objectDetector: TFObjectDetector) : CargoDetector, CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Executors.newSingleThreadExecutor().asCoroutineDispatcher() + job

    override val goldPosition: BroadcastChannel<GoldPosition> =
        broadcastGoldPosition(objectDetector, ticker(50))

    private fun CoroutineScope.broadcastGoldPosition(
        objectDetector: TFObjectDetector,
        ticker: ReceiveChannel<Unit>
    ) = broadcast(capacity = Channel.CONFLATED) {
        objectDetector.activate()
        while (isActive) {
            ticker.receive()
            val recognitions = objectDetector.updatedRecognitions ?: continue
            val gold = recognitions.filter { it.label == LABEL_GOLD_MINERAL }
            val silver = recognitions.filter { it.label == LABEL_SILVER_MINERAL }
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