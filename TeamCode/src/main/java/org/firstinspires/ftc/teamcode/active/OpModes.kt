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


/**
 * A [RobotComponent] that gives the angle that a REV Robotics Potentiometer is at.
 */
class Potentiometer(private val analogInput: AnalogInput) : RobotComponent {

    /**
     * The angle that the potentiometer is located at.
     *
     * This is calculated by finding the percentage of full rotation that the potentiometer is and
     * multiplying that by the number of degrees at full rotation.
     */
    val angle: Double get() = (analogInput.voltage / analogInput.maxVoltage) * 270

    /**
     * For now, don't worry about whats going on here. Just know that this is the magic that makes
     * installing features onto the robot so simple.
     */
    class Configuration : RobotFeatureConfiguration {
        val name: String = "potentiometer"
    }

    /**
     * For now, don't worry about whats going on here. Just know that this is the magic that makes
     * installing features onto the robot so simple.
     */
    companion object FeatureInstaller : RobotFeatureInstaller<Configuration, Potentiometer> {
        override fun install(robot: Robot, configure: Configuration.() -> Unit): Potentiometer {
            val configuration = Configuration().apply(configure)
            return Potentiometer(robot.hardwareMap.get(AnalogInput::class.java, configuration.name))
        }
    }

}

@Autonomous
class LibAutonomous : LinearOpMode() {

    // TODO: Use gyro drives and encoder drives instead of time turns and drives.

    /**
     * The sequence of actions that are performed when the robot detects the gold on the left.
     */
    private val leftAction: RobotAction = sequence(
            timeTurn(425, -0.3), wait(100),
            timeDrive(650, 0.5), wait(100),
            timeTurn(325, 0.45), wait(100),
            timeDrive(550, 0.5), wait(50),
            timeDrive(500, -0.2), wait(100),
            timeTurn(225, 0.4), wait(100),
            timeDrive(700, -0.5)
    )

    /**
     * The sequence of actions that are performed when the robot detects the gold in the center.
     */
    private val centerAction: RobotAction = sequence(
            // timeDrive(1300L, 0.5),
            driveTo(1000, 0.5)

            //timeDrive(300L, -0.3), wait(100),
            //turnTo(-135.0, 0.3), wait(100),
            //timeDrive(500, 0.7)
    )

    /**
     * The sequence of actions that are performed when the robot detects the gold on the right.
     */
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

        // Create the robot and install the [RobotTankDriveTrain], [RobotTankDriveTrain.Localizer],
        // [RobotIMULocalizer] and [Potentiometer]
        val robot = createRobot(this) {
            install(RobotTankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
            install(RobotTankDriveTrain.PositionLocalizer) {
                // TODO: Change the wheel diameter to the actual wheel diameter.
                /* Setting the wheel diameter to 1440/Π makes the localizer return the raw encoder
                value from the motor, which you could find out by doing some simple algebra. */
                wheelDiameter = 1440 / Math.PI
            }
            install(RobotIMULocalizer)
            install(Potentiometer)
        }

        // Run an action that grabs the potentiometer feature from the robot, checks the angle of
        // it and performs the appropriate action.
        robot.runAction {

            // Signify the the robot that the potentiometer is needed in order to run this action.
            val potentiometer = requiredFeature(Potentiometer)

            // Determine the angle of the potentiometer and choose which action to run.
            when (potentiometer.angle) {
                // If the angle is between 0 and 90, run the left action.
                in 0..90 -> robot.runAction(leftAction)

                // If the angle is between 90 and 180, run the center action.
                in 90..180 -> robot.runAction(centerAction)

                // If the angle is between 180 and 270, run the right action.
                in 180..270 -> robot.runAction(rightAction)
            }
        }
    }

}
