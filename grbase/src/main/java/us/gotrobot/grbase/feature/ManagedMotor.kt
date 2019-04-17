package us.gotrobot.grbase.feature

import com.qualcomm.robotcore.hardware.DcMotorEx
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

    @Suppress("EXPERIMENTAL_API_USAGE")
    fun init() {
        val ticker = ticker(100, 0, mode = TickerMode.FIXED_DELAY)
        startUpdatingMotorPowers(ticker)
    }

    fun CoroutineScope.startUpdatingMotorPowers(ticker: ReceiveChannel<Unit>) = launch {
        var outputPower = 0.0
        while (isActive) {
            select<Unit> {
                ticker.onReceive {
                    motor.power = outputPower
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

    companion object Installer : FeatureInstaller<ManagedMotor, Configuration>() {
        override val name: String = "Managed Motor"
        override suspend fun install(
            context: RobotContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): ManagedMotor {
            val configuration = Configuration().apply(configure)
            val motor = context.hardwareMap[DcMotorEx::class, configuration.name]
            return ManagedMotor(motor, context.coroutineScope.coroutineContext).apply {
                init()
            }
        }
    }

    class Configuration : FeatureConfiguration {
        lateinit var name: String
    }

}