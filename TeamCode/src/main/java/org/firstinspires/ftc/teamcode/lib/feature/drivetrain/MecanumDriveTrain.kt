package org.firstinspires.ftc.teamcode.lib.feature.drivetrain

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.TickerMode
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import org.firstinspires.ftc.teamcode.lib.feature.Feature
import org.firstinspires.ftc.teamcode.lib.feature.FeatureConfiguration
import org.firstinspires.ftc.teamcode.lib.feature.FeatureSet
import org.firstinspires.ftc.teamcode.lib.feature.KeyedFeatureInstaller
import org.firstinspires.ftc.teamcode.lib.pipeline.Pipeline
import org.firstinspires.ftc.teamcode.lib.robot.RobotFeatureInstaller
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

class MecanumDriveTrain(
    private val frontLeftMotor: DcMotor,
    private val frontRightMotor: DcMotor,
    private val backLeftMotor: DcMotor,
    private val backRightMotor: DcMotor,
    private val parentContext: CoroutineContext = EmptyCoroutineContext
) : Feature(), OmnidirectionalDriveTrain, InterceptableDriveTrain<MecanumDriveTrain.MotorPowers>,
    CoroutineScope {

    private val job: Job = Job(parentContext[Job])

    override val coroutineContext: CoroutineContext
        get() = parentContext + CoroutineName("Mecanum Drive Train") + job

    private val powerChannel: Channel<MotorPowers> = Channel(Channel.CONFLATED)

    override val powerPipeline = Pipeline<MotorPowers, DriveTrain>()

    data class MotorPowers(
        var frontLeftPower: Double = 0.0,
        var frontRightPower: Double = 0.0,
        var backLeftPower: Double = 0.0,
        var backRightPower: Double = 0.0
    ) : DriveTrainMotorPowers {
        override fun adjustHeadingPower(power: Double) {
            frontLeftPower -= power
            frontRightPower += power
            backLeftPower -= power
            backRightPower += power
        }
    }

    override suspend fun setLinearPower(power: Double) =
        powerChannel.send(MotorPowers(power, power, power, power))

    override suspend fun setLateralPower(power: Double) =
        powerChannel.send(MotorPowers(power, -power, -power, power))

    override suspend fun setRotationalPower(power: Double) =
        powerChannel.send(MotorPowers(-power, power, -power, power))

    override suspend fun setDirectionPower(
        linearPower: Double,
        lateralPower: Double,
        rotationalPower: Double
    ) {
        powerChannel.send(
            MotorPowers(
                frontLeftPower = linearPower + lateralPower + rotationalPower,
                frontRightPower = linearPower - lateralPower - rotationalPower,
                backLeftPower = linearPower - lateralPower + rotationalPower,
                backRightPower = linearPower + lateralPower - rotationalPower
            )
        )
    }

    private fun CoroutineScope.startUpdatingPowers(ticker: ReceiveChannel<Unit>) = launch {
        var targetPowers = MotorPowers()
        while (isActive) {
            select<Unit> {
                powerChannel.onReceive {
                    targetPowers = it
                }
                ticker.onReceive {
                    val (fl, fr, bl, br) = powerPipeline.execute(
                        targetPowers, this@MecanumDriveTrain
                    )
                    frontLeftMotor.power = fl
                    frontRightMotor.power = fr
                    backLeftMotor.power = bl
                    backRightMotor.power = br
                }
            }
        }
    }

    companion object Installer : KeyedFeatureInstaller<MecanumDriveTrain, Configuration>() {

        override val name: String = "Mecanum Drive Train"

        override suspend fun install(
            robot: RobotFeatureInstaller,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): MecanumDriveTrain {
            val config = Configuration().apply(configure)

            val frontLeftMotor =
                robot.hardwareMap.get(DcMotor::class.java, config.frontLeftMotorName)
                    .apply { setup(DcMotorSimple.Direction.REVERSE) }

            val frontRightMotor =
                robot.hardwareMap.get(DcMotor::class.java, config.frontRightMotorName)
                    .apply { setup(DcMotorSimple.Direction.FORWARD) }

            val backLeftMotor =
                robot.hardwareMap.get(DcMotor::class.java, config.backLeftMotorName)
                    .apply { setup(DcMotorSimple.Direction.REVERSE) }

            val backRightMotor =
                robot.hardwareMap.get(DcMotor::class.java, config.backRightMotorName)
                    .apply { setup(DcMotorSimple.Direction.FORWARD) }

            return MecanumDriveTrain(
                frontLeftMotor, frontRightMotor,
                backLeftMotor, backRightMotor,
                coroutineContext
            ).apply {
                @Suppress("EXPERIMENTAL_API_USAGE")
                startUpdatingPowers(ticker(10, 0, this.coroutineContext, TickerMode.FIXED_DELAY))
            }
        }

        private fun DcMotor.setup(direction: DcMotorSimple.Direction) {
            this.direction = direction
            this.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            this.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }

    }

    class Configuration : FeatureConfiguration {
        var frontLeftMotorName: String = "front left motor"
        var frontRightMotorName: String = "front right motor"
        var backLeftMotorName: String = "back left motor"
        var backRightMotorName: String = "back right motor"
    }

}
