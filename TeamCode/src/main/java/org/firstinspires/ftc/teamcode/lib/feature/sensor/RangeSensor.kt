package org.firstinspires.ftc.teamcode.lib.feature.sensor

import com.qualcomm.robotcore.hardware.DistanceSensor
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.hardwareMap

class RangeSensor(
    private val sensor: DistanceSensor
) : Feature {

    fun getDistance(): Double = sensor.getDistance(DistanceUnit.CM)

    class Configuration : FeatureConfiguration {
        var sensorName = "range sensor"
    }

    companion object Installer : FeatureInstaller<Configuration, RangeSensor> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): RangeSensor {
            val configuration = Configuration().apply(configure)
            val sensorName = configuration.sensorName
            val sensor = robot.hardwareMap.get(DistanceSensor::class.java, sensorName)
            return RangeSensor(sensor)
        }
    }

}