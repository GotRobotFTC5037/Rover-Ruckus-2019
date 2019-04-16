//package us.gotrobot.grbase.feature.sensor
//
//import com.qualcomm.robotcore.hardware.DistanceSensor
//import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
//import us.gotrobot.grbase.feature.Feature
//import us.gotrobot.grbase.feature.FeatureConfiguration
//import us.gotrobot.grbase.feature.FeatureInstaller
//import us.gotrobot.grbase.robot.robot
//import us.gotrobot.grbase.robot.hardwareMap
//
//class RangeSensor(
//    private val sensor: DistanceSensor
//) : Feature {
//
//    val distance: Double get() = sensor.getDistance(DistanceUnit.CM)
//
//    class Configuration : FeatureConfiguration {
//        var sensorName = "range sensor"
//    }
//
//    companion object Installer : FeatureInstaller<Configuration, RangeSensor> {
//        override fun install(robot: robot, configure: Configuration.() -> Unit): RangeSensor {
//            val configuration = Configuration().apply(configure)
//            val sensorName = configuration.sensorName
//            val sensor = robot.hardwareMap.get(DistanceSensor::class.java, sensorName)
//            return RangeSensor(sensor)
//        }
//    }
//
//}