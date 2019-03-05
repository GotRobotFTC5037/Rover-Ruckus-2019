//package org.firstinspires.ftc.teamcode.active.old.features
//
//import com.qualcomm.robotcore.hardware.DcMotor
//import com.qualcomm.robotcore.hardware.DcMotorSimple
//import com.qualcomm.robotcore.hardware.TouchSensor
//import kotlinx.coroutines.yield
//import org.firstinspires.ftc.teamcode.lib.feature.Feature
//import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
//import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
//import org.firstinspires.ftc.teamcode.lib.robot.robot
//import org.firstinspires.ftc.teamcode.lib.robot.hardwareMap
//
//const val LIFT_DOWN_POSITION = 15_500
//
//class Lift(
//    private val liftMotor: DcMotor,
//    private val  liftButton: TouchSensor
//) : Feature {
//
//    val liftPosition: Int get() = liftMotor.currentPosition
//
//    fun setPower(power: Double) {
//        if (power < 0.0 && !liftButton.isPressed) {
//            liftMotor.power = power
//        } else if (power >= 0.0) {
//            liftMotor.power = power
//        }
//    }
//
//    suspend fun retract() {
//        liftMotor.power = -1.0
//        while (!liftButton.isPressed) {
//            yield()
//        }
//        liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        liftMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//        liftMotor.power = 0.0
//    }
//
//    suspend fun extend() {
//        liftMotor.power = 1.0
//        while (liftPosition < LIFT_DOWN_POSITION) {
//            yield()
//        }
//        liftMotor.power = 0.0
//    }
//
//    class Configuration : FeatureConfiguration {
//        var liftMotorName: String = "lift motor"
//        var liftButton: String = "lift button"
//    }
//
//    companion object Installer : FeatureInstaller<Configuration, Lift> {
//        override fun install(robot: robot, configure: Configuration.() -> Unit): Lift {
//            val config = Configuration().apply(configure)
//
//            val liftMotor = robot.hardwareMap.get(DcMotor::class.java, config.liftMotorName)
//            liftMotor.direction = DcMotorSimple.Direction.FORWARD
//            liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//            liftMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//            liftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//
//            val liftButton = robot.hardwareMap.get(TouchSensor::class.java, config.liftButton)
//
//            return Lift(liftMotor, liftButton)
//        }
//    }
//}