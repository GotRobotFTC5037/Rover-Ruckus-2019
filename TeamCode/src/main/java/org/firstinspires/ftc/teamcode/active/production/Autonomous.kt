package org.firstinspires.ftc.teamcode.active.production

import android.content.Context
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.Range
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
                val angle =
                    Range.clip(analogInput.voltage / analogInput.maxVoltage * 270, 0.0, 270.0)
                send(angle)
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


    /**
     * The sequence of actions that are performed when the robot detects the gold on the left.
     */
    private val leftAction = actionSequenceOf(
        turnTo(19.0, 0.3) then wait(100),
        driveTo(1050, 0.4),
        turnTo(-19.0, 0.3) then wait(100),
        driveTo(720, 0.4) then wait(1000),
        driveTo(-130, 0.3),
        turnTo(-80.0, 0.3) then wait(100),
        driveTo(710, 0.2),
        turnTo(-120.0, 0.3) then wait(100),
        driveTo(360, 0.5)
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
        turnTo(-19.0, 0.3) then wait(100),
        driveTo(1050, 0.4),
        turnTo(19.0, 0.3) then wait(100),
        driveTo(720, 0.4) then wait(1000),
        driveTo(-130, 0.4),
        turnTo(-110.0, 0.3) then wait(100),
        driveTo(1800, 0.5)
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() {

        val robot = robot(this) {
            install(TankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
            install(IMULocalizer)
            install(Potentiometer)
            install(TankDriveTrain.Localizer)
            //install(GoldPositionDetector)
        }.perform {
            // Checkout the potentiometer.
            val positionDetector = requestFeature(Potentiometer)

            // Subscribe to the angle broadcasting channel.
            val positionChannel = positionDetector.angle.openSubscription()
            val currentPosition = positionChannel.receive()
            positionChannel.cancel()

            telemetry.addLine("Angle: $currentPosition")
            telemetry.update()

            // Check the potentiometer angle and choose which action to run.
            when (currentPosition) {
                in 0.0..90.0 -> perform(leftAction)
                in 90.0..180.0 -> perform(centerAction)
                in 180.0..270.0 -> perform(rightAction)
            }
        }
    }

}


