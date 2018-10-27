package org.firstinspires.ftc.teamcode.active.production

import android.content.Context
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.AnalogInput
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

    /**
     * The sequence of actions that are performed when the robot detects the gold on the left.
     */
    private val leftAction = actionSequenceOf(
        timeTurn(425, -0.3) then wait(100),
        timeDrive(650, 0.5) then wait(100),
        timeTurn(325, 0.45) then wait(100),
        timeDrive(550, 0.5) then wait(50),
        timeDrive(500, -0.2) then wait(100),
        timeTurn(225, 0.4) then wait(100),
        timeDrive(700, -0.5)
    )

    /**
     * The sequence of actions that are performed when the robot detects the gold in the center.
     */
    private val centerAction = actionSequenceOf(
        timeDrive(1300L, 0.5),
        timeDrive(300L, -0.3) then wait(100),
        turnTo(-135.0, 0.3) then wait(100),
        timeDrive(500, 0.7)
    )

    /**
     * The sequence of actions that are performed when the robot detects the gold on the right.
     */
    private val rightAction = actionSequenceOf(
        timeTurn(400, 0.3),
        timeDrive(875, 0.45) then wait(100),
        timeTurn(300, -0.4),
        timeDrive(650, 0.5),
        timeTurn(550, -0.5) then wait(100),
        timeDrive(450, 0.7),
        timeTurn(100, -0.30),
        timeDrive(500, 0.7)
    )

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        robot(this) {
            install(TankDriveTrain) {
                addLeftMotor("left motor")
                addRightMotor("right motor")
            }
            install(IMULocalizer)
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
        }
    }

}


