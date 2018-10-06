package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.util.Range
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.produce

class Archimedes(
    private val linearOpMode: LinearOpMode,
    private val coroutineScope: CoroutineScope
) {

    var shouldRecordBallLauncherIntegral = true

    val leftMotor: DcMotor by lazy {
        linearOpMode.hardwareMap.dcMotor.get("left motor").apply {
            direction = DcMotorSimple.Direction.FORWARD
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        }
    }

    val rightMotor: DcMotor by lazy {
        linearOpMode.hardwareMap.dcMotor.get("right motor").apply {
            direction = DcMotorSimple.Direction.REVERSE
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        }
    }

    val ballCollector: DcMotor by lazy {
        linearOpMode.hardwareMap.dcMotor.get("ball collector").apply {
            direction = DcMotorSimple.Direction.REVERSE
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        }
    }

    val ballDeployer: Servo by lazy {
        linearOpMode.hardwareMap.servo.get("ball deployer").apply {
            direction = Servo.Direction.REVERSE
        }
    }

    private val ballLauncher: DcMotor by lazy {
        linearOpMode.hardwareMap.dcMotor.get("ball launcher").apply {
            direction = DcMotorSimple.Direction.REVERSE
            mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
        }
    }

    private fun CoroutineScope.ballLauncherSpeed() = produce {
        while (isActive) {
            val initialPosition = ballLauncher.currentPosition
            delay(50)
            val finalPosition = ballLauncher.currentPosition
            send((finalPosition - initialPosition) / 0.05)
        }
    }

    suspend fun startBallLauncher() = coroutineScope.launch {
        val ballLauncherSpeed = ballLauncherSpeed()
        var ballLauncherSpeedIntegral = 0.0
        var previousBallLauncherSpeedError = 0.0

        while (isActive) {
            val deltaTime = 0.05
            val currentSpeed = ballLauncherSpeed.receive()

            val ballLauncherSpeedError = DEFAULT_BALL_LAUNCHER_SPEED - currentSpeed
            if (shouldRecordBallLauncherIntegral)
                ballLauncherSpeedIntegral += ballLauncherSpeedError * deltaTime
            val ballLauncherSpeedDerivative =
                (ballLauncherSpeedError - previousBallLauncherSpeedError) / deltaTime

            val p = ballLauncherSpeedError * BALL_LAUNCHER_SPEED_PROPORTIONAL_GAIN
            val i = ballLauncherSpeedIntegral * BALL_LAUNCHER_SPEED_INTEGRAL_GAIN
            val d = ballLauncherSpeedDerivative * BALL_LAUNCHER_SPEED_DERIVATIVE_GAIN
            ballLauncher.power = Range.clip(p + i + d, -1.0, 1.0)

            previousBallLauncherSpeedError = ballLauncherSpeedError

            yield()
        }

        ballLauncher.power = 0.0
    }


    companion object {
        private const val DEFAULT_BALL_LAUNCHER_SPEED = 1650
        private const val BALL_LAUNCHER_SPEED_PROPORTIONAL_GAIN = 0.00046000
        private const val BALL_LAUNCHER_SPEED_INTEGRAL_GAIN = 0.00075000
        private const val BALL_LAUNCHER_SPEED_DERIVATIVE_GAIN = 0.000005225
    }

}
