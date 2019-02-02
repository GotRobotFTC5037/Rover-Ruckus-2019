package org.firstinspires.ftc.teamcode.active.features

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Servo
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.hardwareMap

class CargoDeliverySystem(
    private val intakeLiftMotor: DcMotor,
    private val intakeMotor: DcMotor,
    private val popperMotor: DcMotor,
    private val chuteLiftMotor: DcMotor,
    private val chuteShutterServo: Servo
) : Feature {

    val intake: Intake = Intake()
    val chute: Chute = Chute()
    val popper: Popper = Popper()

    inner class Intake {
        fun setLiftPower(power: Double) {
            intakeLiftMotor.power = power
        }

        fun setIntakePower(power: Double) {
            intakeMotor.power = power
        }
    }

    inner class Chute {
        fun dropShutter() {
            chuteShutterServo.position = 0.0
        }

        fun raiseShutter() {
            chuteShutterServo.position = 1.0
        }

        fun setChuteLiftPower(power: Double) {
            chuteLiftMotor.power = power
        }
    }

    inner class Popper {
        fun enablePopper() {
            popperMotor.power = 1.0
        }

        fun disablePopper() {
            popperMotor.power = 0.0
        }

        suspend fun popperDistance(distance: Int) {
            val initialPosition = popperMotor.currentPosition
            popperMotor.power = 1.0
            while (initialPosition + distance < popperMotor.currentPosition) {
                yield()
            }
            popperMotor.power = 0.0
        }
    }

    class Configuration : FeatureConfiguration {
        var intakeLift = "intake lift"
        var intake = "intake"
        var chuteLift = "chute lift"
        var popper = "popper"
        var shutter = "shutter"
    }

    companion object Installer : FeatureInstaller<Configuration, CargoDeliverySystem> {
        override fun install(
            robot: Robot,
            configure: Configuration.() -> Unit
        ): CargoDeliverySystem {
            val configuration = Configuration().apply(configure)
            val lift = robot.hardwareMap.get(DcMotor::class.java, configuration.intakeLift).apply {
                zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                power = 0.1
            }
            val intake = robot.hardwareMap.get(DcMotor::class.java, configuration.intake)
            val chuteLift =
                robot.hardwareMap.get(DcMotor::class.java, configuration.chuteLift).apply {
                    direction = DcMotorSimple.Direction.FORWARD
                    zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                }
            val popper = robot.hardwareMap.get(DcMotor::class.java, configuration.popper)
            val shutter = robot.hardwareMap.get(Servo::class.java, configuration.shutter).apply {
                this.scaleRange(0.0, 0.75)
            }
            return CargoDeliverySystem(lift, intake, popper, chuteLift, shutter)
        }
    }
}