package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.AnalogInput
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.RobotComponent
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.RobotFeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.RobotTankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.RobotIMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.createRobot

class Potentiometer(private val analogInput: AnalogInput) : RobotComponent {

    val angle: Double
        get() = (analogInput.voltage / analogInput.maxVoltage) * 270

    class Configuration : RobotFeatureConfiguration {
        val name: String = "potentiometer"
    }

    companion object FeatureInstaller : RobotFeatureInstaller<Configuration, Potentiometer> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): Potentiometer {
            val configuration = Configuration().apply(configure)
            return Potentiometer(robot.hardwareMap.get(AnalogInput::class.java, configuration.name))
        }
    }

}

@Autonomous
class LibAutonomous : LinearOpMode() {

    private val leftAction: RobotAction = sequence(
        timeTurn(425, -0.3), wait(100),
        timeDrive(650, 0.5), wait(100),
        timeTurn(325, 0.45), wait(100),
        timeDrive(550, 0.5), wait(50),
        timeDrive(500, -0.2), wait(100),
        timeTurn(225, 0.4), wait(100),
        timeDrive(700, -0.5)
    )

    private val centerAction: RobotAction = sequence(
        timeDrive(1300L, 0.5),
        timeDrive(300L, -0.3), wait(100),
        turnTo(-135.0, 0.3), wait(100),
        timeDrive(500, 0.7)
    )

    private val rightAction: RobotAction = sequence(
        timeTurn(400, 0.3),
        timeDrive(875, 0.45), wait(100),
        timeTurn(300, -0.4),
        timeDrive(650, 0.5),
        timeTurn(550, -0.5), wait(100),
        timeDrive(450, 0.7),
        timeTurn(100, -0.30),
        timeDrive(500, 0.7)
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        val robot = createRobot(this) {
            install(RobotTankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
            install(RobotTankDriveTrain.PositionLocalizer)
            install(RobotIMULocalizer)
            install(Potentiometer)
        }

        robot.runAction {
            val potentiometer = requiredFeature(Potentiometer)

            when (potentiometer.angle) {
                in 0..90 -> robot.runAction(leftAction)
                in 91..180 -> robot.runAction(centerAction)
                in 181..270 -> robot.runAction(rightAction)
            }
        }
    }

}
