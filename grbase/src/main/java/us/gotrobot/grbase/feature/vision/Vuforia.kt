package us.gotrobot.grbase.feature.vision

import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import us.gotrobot.grbase.feature.Feature
import us.gotrobot.grbase.feature.FeatureConfiguration
import us.gotrobot.grbase.feature.FeatureSet
import us.gotrobot.grbase.feature.KeyedFeatureInstaller
import us.gotrobot.grbase.robot.RobotContext
import us.gotrobot.grbase.util.get

class Vuforia(
    private val licenceKey: String,
    private val webcamName: WebcamName
) : Feature() {

    lateinit var localizer: VuforiaLocalizer

    fun init() {
        val parameters = VuforiaLocalizer.Parameters().apply {
            this.vuforiaLicenseKey = licenceKey
            this.cameraDirection = VuforiaLocalizer.CameraDirection.BACK
            this.cameraName = webcamName
        }
        localizer = ClassFactory.getInstance().createVuforia(parameters)
    }

    companion object Installer : KeyedFeatureInstaller<Vuforia, Configuration>() {
        override val name: String = "Vuforia"
        override suspend fun install(
            context: RobotContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): Vuforia {
            val configuration = Configuration().apply(configure)
            val webcamName = context.hardwareMap[WebcamName::class, configuration.cameraName]
            val vuforia = Vuforia(configuration.licenceKey, webcamName)
            vuforia.init()
            return vuforia
        }
    }

    class Configuration : FeatureConfiguration {
        lateinit var licenceKey: String
        lateinit var cameraName: String
    }

}
