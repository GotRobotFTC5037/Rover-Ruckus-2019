package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.hardwareMap

class MarkerDeployer(private val servo: Servo) : Feature {

    fun deploy() {
        servo.position = 1.0
    }

    fun retract() {
        servo.position = 0.0
    }

    class Configuration : FeatureConfiguration {
        var servoName = "marker deployer"
    }

    companion object Installer : FeatureInstaller<Configuration, MarkerDeployer> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): MarkerDeployer {
            val configuration = Configuration().apply(configure)
            val servo = robot.hardwareMap.get(Servo::class.java, configuration.servoName)
            servo.scaleRange(0.0, 0.8)
            return MarkerDeployer(servo)
        }
    }
}
