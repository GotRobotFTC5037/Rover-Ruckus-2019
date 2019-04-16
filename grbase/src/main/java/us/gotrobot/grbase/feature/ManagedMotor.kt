package us.gotrobot.grbase.feature

import com.qualcomm.robotcore.hardware.DcMotorEx
import us.gotrobot.grbase.robot.RobotContext
import us.gotrobot.grbase.util.get

class ManagedMotor(
    val motor: DcMotorEx
) : Feature() {



    companion object Installer : FeatureInstaller<ManagedMotor, Configuration>() {
        override val name: String = "Managed Motor"
        override suspend fun install(
            context: RobotContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): ManagedMotor {
            val configuration = Configuration().apply(configure)
            val motor = context.hardwareMap[DcMotorEx::class, configuration.name]
            return ManagedMotor(motor)
        }
    }

    class Configuration : FeatureConfiguration {
        lateinit var name: String
    }
}