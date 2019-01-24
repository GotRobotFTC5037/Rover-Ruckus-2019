package org.firstinspires.ftc.teamcode.active.features

import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.hardwareMap

class Popper(private val motor: DcMotor): Feature {

    fun activate() {
        motor.power = 1.0
    }

    fun deactivate() {
        motor.power = 0.0
    }

    class Configuration : FeatureConfiguration {
        var motorName = "popper"
    }

    companion object Installer : FeatureInstaller<Configuration, Popper> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): Popper {
            val configuration = Configuration().apply(configure)
            return Popper(robot.hardwareMap.get(DcMotor::class.java, configuration.motorName))
        }
    }
}