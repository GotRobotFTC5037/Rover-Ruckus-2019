package us.gotrobot.grbase.feature

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.TickerMode
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import us.gotrobot.grbase.robot.RobotContext
import us.gotrobot.grbase.util.get
import kotlin.coroutines.CoroutineContext

class ManagedMotor(
    private val motor: DcMotorEx,
    parentContext: CoroutineContext
) : Feature(), CoroutineScope {

    private val job = Job(parentContext[Job])

    override val coroutineContext: CoroutineContext = parentContext + job + CoroutineName("Managed Motor")

    private val powerChannel = Channel<Double>(Channel.CONFLATED)
    private val positionChannel = Channel<Int>(Channel.CONFLATED)

    @Suppress("EXPERIMENTAL_API_USAGE")
    fun init() {
        val ticker = ticker(100, 0, mode = TickerMode.FIXED_DELAY)
        startUpdatingMotorPowers(ticker)
    }

    private fun CoroutineScope.startUpdatingMotorPowers(ticker: ReceiveChannel<Unit>) = launch {
        var outputPower = 0.0
        var targetPosition = 0
        while (isActive) {
            select<Unit> {
                ticker.onReceive {
                    motor.power = outputPower
                    positionChannel.offer(motor.currentPosition)
                }
                powerChannel.onReceive {
                    outputPower = it
                }
            }
        }
    }

    fun setPower(power: Double) {
        powerChannel.offer(power)
    }

    suspend fun setPosition(targetPosition: Int) {
        val currentPosition = positionChannel.receive()
        if (currentPosition > targetPosition) {
            while (positionChannel.receive() > targetPosition) {
                setPower(-1.0)
                yield()
            }
        } else if (currentPosition < targetPosition) {
            while (positionChannel.receive() < targetPosition) {
                telemetry.addData("Position", positionChannel.receive())
                telemetry.update()
                setPower(1.0)
                yield()
            }
        }
        setPower(0.0)
    }

    companion object Installer : FeatureInstaller<ManagedMotor, Configuration>() {
        override val name: String = "Managed Motor"
        override suspend fun install(
            context: RobotContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): ManagedMotor {
            val configuration = Configuration().apply(configure)
            val motor = context.hardwareMap[DcMotorEx::class, configuration.name].apply {
                direction = configuration.direction
                zeroPowerBehavior = configuration.zeroPowerBehavior
                mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
                delay(1000)
                mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            }
            return ManagedMotor(motor, context.coroutineScope.coroutineContext).apply {
                init()
            }
        }
    }

    class Configuration : FeatureConfiguration {
        lateinit var name: String
        var direction: DcMotorSimple.Direction = DcMotorSimple.Direction.FORWARD
        var zeroPowerBehavior: DcMotor.ZeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
    }

}