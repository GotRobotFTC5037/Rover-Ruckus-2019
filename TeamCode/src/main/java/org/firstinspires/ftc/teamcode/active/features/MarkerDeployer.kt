package org.firstinspires.ftc.teamcode.active.features

import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot

class MarkerDeployer(private val servo: Servo) : Feature {

    fun retract() {
        servo.position = 0.0
    }

    fun deploy() {
        servo.position = 0.0
    }

    class Configuration : FeatureConfiguration {
        val servoName = "marker deployer"
    }

    companion object Installer : FeatureInstaller<Configuration, MarkerDeployer> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): MarkerDeployer {
            val configuration = Configuration().apply(configure)
            val servo = robot.hardwareMap.get(Servo::class.java, configuration.servoName)
            return MarkerDeployer(servo)
        }
    }
}