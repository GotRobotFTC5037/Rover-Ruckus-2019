package org.firstinspires.ftc.teamcode

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.tfod.Recognition
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector
import us.gotrobot.grbase.feature.Feature
import us.gotrobot.grbase.feature.FeatureConfiguration
import us.gotrobot.grbase.feature.FeatureSet
import us.gotrobot.grbase.feature.KeyedFeatureInstaller
import us.gotrobot.grbase.feature.vision.Vuforia
import us.gotrobot.grbase.robot.RobotContext
import kotlin.coroutines.CoroutineContext

class CargoDetector(
    private val vuforia: Vuforia,
    private val appContext: Context,
    private val parentContext: CoroutineContext
) : Feature(), CoroutineScope {

    private val job = Job(parentContext[Job])

    override val coroutineContext: CoroutineContext
        get() = CoroutineName("Cargo Detector") + parentContext + job

    private lateinit var objectDetector: TFObjectDetector

    private val recognitionsChannel = Channel<List<Recognition>>(Channel.CONFLATED)
    private val _goldPositionChannel = Channel<GoldPosition>(Channel.CONFLATED)
    val goldPositionChannel: ReceiveChannel<GoldPosition> = _goldPositionChannel

    enum class GoldPosition {
        LEFT, CENTER, RIGHT, UNKNOWN
    }

    fun CoroutineScope.updateRecognitions() = launch {
        while (isActive) {
            val recognitions = objectDetector.updatedRecognitions
            if (recognitions != null) {
                recognitionsChannel.offer(recognitions)
            } else {
                yield()
            }
        }
    }

    private val Recognition.isGold: Boolean get() = label == LABEL_GOLD_MINERAL
    private val Recognition.isSilver: Boolean get() = label == LABEL_SILVER_MINERAL

    fun CoroutineScope.updateGoldPosition() = launch {
        while (isActive) {
            val recognitions = recognitionsChannel.receive()
            val goldRecognitions = recognitions
                .filter { it.isGold }
                .sortedBy { it.width }
            val silverRecognitions = recognitions
                .filter { it.isSilver }
                .sortedBy { it.width }

            if (goldRecognitions.count() >= 1 && silverRecognitions.count() >= 2) {
                _goldPositionChannel.offer(GoldPosition.CENTER)
            } else {
                _goldPositionChannel.offer(GoldPosition.UNKNOWN)
            }
        }
    }

    fun init() {
        val tfodMonitorViewId = appContext.resources
            .getIdentifier("tfodMonitorViewId", "id", appContext.packageName)
        val parameters = TFObjectDetector.Parameters(tfodMonitorViewId)
        val classFactory = ClassFactory.getInstance()
        objectDetector = classFactory.createTFObjectDetector(parameters, vuforia.localizer)
        objectDetector.loadModelFromAsset(
            TFOD_MODEL_ASSET,
            LABEL_GOLD_MINERAL,
            LABEL_SILVER_MINERAL
        )
        objectDetector.activate()
        updateRecognitions()
        updateGoldPosition()
    }

    suspend fun shutdown() = withContext(Dispatchers.IO) {
        objectDetector.shutdown()
    }

    companion object Installer : KeyedFeatureInstaller<CargoDetector, Configuration>() {

        private const val TFOD_MODEL_ASSET = "RoverRuckus.tflite"
        private const val LABEL_GOLD_MINERAL = "Gold Mineral"
        private const val LABEL_SILVER_MINERAL = "Silver Mineral"

        override val name: String = "Cargo Detector"

        override suspend fun install(
            context: RobotContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): CargoDetector {
            val configuration = Configuration().apply(configure)
            val vuforia = configuration.vuforia
            val appContext = context.hardwareMap.appContext
            val parentCoroutineContext = context.coroutineScope.coroutineContext
            return CargoDetector(vuforia, appContext, parentCoroutineContext).apply {
                this.init()
            }
        }

    }

    class Configuration : FeatureConfiguration {
        lateinit var vuforia: Vuforia
    }
}