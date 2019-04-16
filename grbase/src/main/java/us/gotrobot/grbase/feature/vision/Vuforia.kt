package us.gotrobot.grbase.feature.vision

import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import us.gotrobot.grbase.feature.Feature
import us.gotrobot.grbase.feature.FeatureConfiguration
import us.gotrobot.grbase.feature.FeatureSet
import us.gotrobot.grbase.feature.KeyedFeatureInstaller
import us.gotrobot.grbase.robot.RobotContext

class Vuforia(private val licenceKey: String) : Feature() {

    lateinit var localizer: VuforiaLocalizer

    fun init() {
        val parameters = VuforiaLocalizer.Parameters().apply {
            this.vuforiaLicenseKey = licenceKey
            this.cameraDirection = VuforiaLocalizer.CameraDirection.BACK
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
            val vuforia = Vuforia(configuration.licenceKey)
            vuforia.init()
            return vuforia
        }
    }

    class Configuration : FeatureConfiguration {
        lateinit var licenceKey: String
    }

}