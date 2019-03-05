package org.firstinspires.ftc.teamcode.lib.feature.drivetrain

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.selects.select
import org.firstinspires.ftc.teamcode.lib.pipeline.ChannelPipeline
import org.firstinspires.ftc.teamcode.lib.feature.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

class MecanumDriveTrain(
    private val frontLeftMotor: DcMotor,
    private val frontRightMotor: DcMotor,
    private val backLeftMotor: DcMotor,
    private val backRightMotor: DcMotor,
    private val parentContext: CoroutineContext = EmptyCoroutineContext
) : Feature(), OmnidirectionalDriveTrain, CoroutineScope {

    private val job: Job = Job(parentContext[Job])

    override val coroutineContext: CoroutineContext
        get() = parentContext + CoroutineName("Mecanum Drive Train") + job

    private lateinit var powerUpdateJob: Job

    private val powerChannel: Channel<MotorPowers> = Channel(Channel.CONFLATED)

    val powerPipeline: ChannelPipeline<MotorPowers> = ChannelPipeline()

    data class MotorPowers(
        val frontLeftPower: Double = 0.0,
        val frontRightPower: Double = 0.0,
        val backLeftPower: Double = 0.0,
        val backRightPower: Double = 0.0
    )

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
                    val (fl, fr, bl, br) = powerPipeline.execute(targetPowers.copy())
                    frontLeftMotor.power = fl
                    frontRightMotor.power = fr
                    backLeftMotor.power = bl
                    backRightMotor.power = br
                }
            }
        }
    }

    companion object Installer : KeyedFeatureInstaller<MecanumDriveTrain, Configuration>() {

        override val featureName: String = "Mecanum Drive Train"

        override suspend fun install(
            hardwareMap: HardwareMap,
            configure: Configuration.() -> Unit
        ): MecanumDriveTrain {
            val config = Configuration().apply(configure)

            val frontLeftMotor = hardwareMap.get(DcMotor::class.java, config.frontLeftMotorName)
                .apply { setup(DcMotorSimple.Direction.REVERSE) }
            val frontRightMotor = hardwareMap.get(DcMotor::class.java, config.frontRightMotorName)
                .apply { setup(DcMotorSimple.Direction.FORWARD) }
            val backLeftMotor = hardwareMap.get(DcMotor::class.java, config.backLeftMotorName)
                .apply { setup(DcMotorSimple.Direction.REVERSE) }
            val backRightMotor = hardwareMap.get(DcMotor::class.java, config.backRightMotorName)
                .apply { setup(DcMotorSimple.Direction.FORWARD) }

            return MecanumDriveTrain(
                frontLeftMotor, frontRightMotor,
                backLeftMotor, backRightMotor,
                coroutineContext
            )
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
