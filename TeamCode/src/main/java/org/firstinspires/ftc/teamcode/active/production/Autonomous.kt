package org.firstinspires.ftc.teamcode.active.production

import android.content.Context
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.corningrobotics.enderbots.endercv.CameraViewDisplay
import org.corningrobotics.enderbots.endercv.OpenCVPipeline
import org.firstinspires.ftc.teamcode.lib.action.*
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureInstaller
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import org.firstinspires.ftc.teamcode.lib.robot.perform
import org.firstinspires.ftc.teamcode.lib.robot.robot
import org.opencv.core.Mat
import kotlin.coroutines.CoroutineContext

/**
 * A [Feature] that gives the angle that a REV Robotics Potentiometer is at.
 */
class Potentiometer(
    private val analogInput: AnalogInput,
    override val coroutineContext: CoroutineContext
) : Feature, CoroutineScope {

    private fun CoroutineScope.broadcastAngle(ticker: ReceiveChannel<Unit>) =
        broadcast(capacity = Channel.CONFLATED) {
            while (isActive) {
                ticker.receive()
                send(analogInput.voltage / analogInput.maxVoltage * 270)
            }
        }

    val angle: BroadcastChannel<Double> =
        broadcastAngle(ticker(10, mode = TickerMode.FIXED_DELAY))

    class Configuration : FeatureConfiguration {
        val name: String = "potentiometer"
    }

    companion object Installer : FeatureInstaller<Configuration, Potentiometer> {
        override fun install(
            robot: Robot,
            hardwareMap: HardwareMap,
            coroutineContext: CoroutineContext,
            configure: Configuration.() -> Unit
        ): Potentiometer {
            val configuration = Configuration().apply(configure)
            val analogInput = hardwareMap.get(AnalogInput::class.java, configuration.name)
            return Potentiometer(analogInput, coroutineContext)
        }
    }

}

class GoldPositionDetector(
    private val appContext: Context,
    private val cameraViewDisplay: CameraViewDisplay,
    override val coroutineContext: CoroutineContext
) : Feature, CoroutineScope {

    enum class Position {
        LEFT, CENTER, RIGHT
    }

    private val pipeline = object : OpenCVPipeline(), CoroutineScope {

        private lateinit var job: Job

        override val coroutineContext: CoroutineContext
            get() = newSingleThreadContext("GoldPositionDetectorPipeline") + job

        override fun onCameraViewStarted(width: Int, height: Int) {
            job = Job()
        }

        override fun onCameraViewStopped() {
            job.cancel()
        }

        fun CoroutineScope.broadcastPosition() = broadcast<Position> {
            TODO()
        }

        override fun processFrame(rgba: Mat?, gray: Mat?): Mat = runBlocking {
            TODO()
        }

    }

    val position: BroadcastChannel<Position> = TODO()

    companion object Installer : FeatureInstaller<Nothing, GoldPositionDetector> {
        override fun install(
            robot: Robot,
            hardwareMap: HardwareMap,
            coroutineContext: CoroutineContext,
            configure: Nothing.() -> Unit
        ): GoldPositionDetector {
            val appContext = hardwareMap.appContext
            val cameraViewDisplay = CameraViewDisplay.getInstance()
            return GoldPositionDetector(appContext, cameraViewDisplay, coroutineContext)
        }
    }

}

@Autonomous
class Autonomous : LinearOpMode() {

    val touchSensor = hardwareMap.get(DigitalChannel::class.java, "Program Selector")
        .apply { mode = DigitalChannel.Mode.INPUT }
    /**
     * The sequence of actions that are performed when the robot detects the gold on the left.
     */
    private val leftAction = actionSequenceOf(
        turnTo(35.0, 0.3) then wait(100),
        driveTo(720, 0.4),
        turnTo(-10.0, 0.3) then wait(100),
        driveTo(1080, 0.4),
        driveTo(-200, 0.4),
        turnTo(-110.0, 0.3) then wait(100),
        driveTo(1800, 0.6)

    )

    /**
     * The sequence of actions that are performed when the robot detects the gold in the center.
     */
    private val centerAction = actionSequenceOf(
        driveTo(1801, 0.4),
        driveTo(-200, 0.4) then wait(100),
        turnTo(-110.0, 0.3) then wait(100)
    )

    /**
     * The sequence of actions that are performed when the robot detects the gold on the right.
     */
    private val rightAction = actionSequenceOf(
        turnTo(-35.0, 0.3) then wait(100),
        driveTo(720, 0.4),
        turnTo(10.0, 0.3) then wait(100),
        driveTo(1080, 0.4),
        driveTo(-200, 0.4),
        turnTo(-110.0, 0.3) then wait(100),
        driveTo(1800, 0.5)


    )

    val main = action {
        // Checkout the potentiometer.
        val potentiometer = requestFeature(Potentiometer)

        // Subscribe to the angle broadcasting channel.
        val angleChannel = potentiometer.angle.openSubscription()

        // Check the potentiometer angle and choose which action to run.
        when (angleChannel.receive()) {
            in 0.0..90.0 -> perform(leftAction)
            in 90.0..180.0 -> perform(centerAction)
            in 180.0..270.0 -> perform(rightAction)
        }

        // Close the angle channel and unsubscribe from the channel.
        angleChannel.cancel()
    }

    @Throws(InterruptedException::class)
    override fun runOpMode() {

        val robot = robot(this) {
            install(TankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
            install(IMULocalizer)
<<<<<<< HEAD
            install(Potentiometer)
        }

        if (touchSensor.state == true) {
        // Run Crater code.
        }
        /*
         Else run main program. -Note: This does not affect the robot AT ALL.
          Until we make the crater code, the space above will not be used.
          */
        else {
            robot.perform(main)
=======
            install(GoldPositionDetector)
        }.perform {
            // Checkout the potentiometer.
            val positionDetector = requestFeature(GoldPositionDetector)

            // Subscribe to the angle broadcasting channel.
            val positionChannel = positionDetector.position.openSubscription()
            val currentPosition = positionChannel.receive()
            positionChannel.cancel()

            // Check the potentiometer angle and choose which action to run.
            when (currentPosition) {
                GoldPositionDetector.Position.LEFT -> perform(leftAction)
                GoldPositionDetector.Position.CENTER -> perform(centerAction)
                GoldPositionDetector.Position.RIGHT -> perform(rightAction)
            }
>>>>>>> 291607e7f3c5f55892360ff2d8f205a662e09bf8
        }
    }

}


