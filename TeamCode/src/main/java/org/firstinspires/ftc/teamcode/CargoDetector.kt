package org.firstinspires.ftc.teamcode

import android.content.Context
import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector
import us.gotrobot.grbase.feature.Feature
import us.gotrobot.grbase.feature.FeatureConfiguration
import us.gotrobot.grbase.feature.FeatureSet
import us.gotrobot.grbase.feature.KeyedFeatureInstaller
import us.gotrobot.grbase.feature.vision.Vuforia
import us.gotrobot.grbase.robot.RobotContext


private val TFOD_MODEL_ASSET = "RoverRuckus.tflite"
private val LABEL_GOLD_MINERAL = "Gold Mineral"
private val LABEL_SILVER_MINERAL = "Silver Mineral"

class CargoDetector(
    private val vuforia: Vuforia,
    private val appContext: Context
) : Feature() {

    private lateinit var objectDetector: TFObjectDetector

    fun init() {
        val tfodMonitorViewId = appContext.resources.getIdentifier(
            "tfodMonitorViewId", "id", appContext.packageName
        )
        val parameters = TFObjectDetector.Parameters(tfodMonitorViewId)
        objectDetector =
            ClassFactory.getInstance().createTFObjectDetector(parameters, vuforia.localizer)

        objectDetector.loadModelFromAsset(
            TFOD_MODEL_ASSET,
            LABEL_GOLD_MINERAL,
            LABEL_SILVER_MINERAL
        )

    }

    companion object Installer : KeyedFeatureInstaller<CargoDetector, Configuration>() {
        override val name: String = "Cargo Detector"
        override suspend fun install(
            context: RobotContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): CargoDetector {
            val configuration = Configuration().apply(configure)
            val vuforia = configuration.vuforia
            val cargoDetector = CargoDetector(vuforia, context.hardwareMap.appContext)
            cargoDetector.init()
            return cargoDetector
        }
    }

    class Configuration : FeatureConfiguration {
        lateinit var vuforia: Vuforia
    }
}