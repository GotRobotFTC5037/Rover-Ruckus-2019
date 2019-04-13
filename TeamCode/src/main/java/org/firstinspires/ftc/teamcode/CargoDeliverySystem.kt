package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.ServoImplEx
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.TickerMode
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import us.gotrobot.grbase.feature.Feature
import us.gotrobot.grbase.feature.FeatureConfiguration
import us.gotrobot.grbase.feature.FeatureSet
import us.gotrobot.grbase.feature.KeyedFeatureInstaller
import us.gotrobot.grbase.robot.FeatureInstallContext
import us.gotrobot.grbase.util.get
import kotlin.coroutines.CoroutineContext


class CargoDeliverySystem(
    private val extensionMotor: DcMotorEx,
    private val rotationMotor: DcMotorEx,
    private val intakeMotor: DcMotorEx,
    private val sortingServo: ServoImplEx,
    private val parentContext: CoroutineContext
) : Feature(), CoroutineScope {

    private val job: Job = Job(parentContext[Job])

    override val coroutineContext: CoroutineContext
        get() = parentContext + CoroutineName("Cargo Delivery System") + job

    private val rotationPowerChannel = Channel<Double>(Channel.CONFLATED)
    private val extensionPowerChannel = Channel<Double>(Channel.CONFLATED)

    enum class SortingDirection {
        LEFT, RIGHT
    }

    private var sortingDirection: SortingDirection =
        SortingDirection.LEFT

    @Suppress("EXPERIMENTAL_API_USAGE")
    fun startPowerUpdates() {
        startRetainingSetPosition(
            rotationMotor,
            rotationPowerChannel,
            ticker(100L, 0L, mode = TickerMode.FIXED_PERIOD),
            0.0015
        )
        startRetainingSetPosition(
            extensionMotor,
            extensionPowerChannel,
            ticker(100L, 0L, mode = TickerMode.FIXED_PERIOD),
            0.0015
        )
    }

    fun setSortingDirection(direction: SortingDirection) {
        sortingDirection = direction
        sortingServo.position = when (direction) {
            SortingDirection.LEFT -> 0.0
            SortingDirection.RIGHT -> 1.0
        }
    }

    private fun CoroutineScope.startRetainingSetPosition(
        motor: DcMotor,
        powerChannel: ReceiveChannel<Double>,
        ticker: ReceiveChannel<Unit>,
        coefficient: Double
    ) = launch {
        var outputPower = 0.0
        var lastDesiredPower = 0.0
        var targetPosition = 0
        while (true) {
            select<Unit> {
                powerChannel.onReceive {
                    outputPower = if (it == 0.0 && lastDesiredPower != 0.0) {
                        targetPosition = motor.currentPosition
                        0.0
                    } else if (it == 0.0) {
                        (targetPosition - motor.currentPosition) * coefficient
                    } else it
                    lastDesiredPower = it
                }
                ticker.onReceive {
                    motor.power = outputPower
                }
            }
        }
    }

    fun setRotationMotorPower(power: Double) {
        rotationPowerChannel.offer(power)
    }

    fun setExtensionMotorPower(power: Double) {
        extensionPowerChannel.offer(power)
    }

    enum class IntakeStatus {
        STOPPED, ADMIT, EJECT
    }

    fun setIntakeStatus(status: IntakeStatus) {
        intakeMotor.power = when (status) {
            IntakeStatus.STOPPED -> 0.0
            IntakeStatus.ADMIT -> 1.0
            IntakeStatus.EJECT -> -1.0
        }
    }

    suspend fun setRotationalMotorPosition(position: Int) {
        val currentPosition = rotationMotor.currentPosition
        if (currentPosition > position) {
            while (rotationMotor.currentPosition > position) {
                setRotationMotorPower(-0.85)
                yield()
            }
        } else if (currentPosition < position) {
            while (rotationMotor.currentPosition < position) {
                setRotationMotorPower(0.85)
                yield()
            }
        }
    }

    companion object Installer : KeyedFeatureInstaller<CargoDeliverySystem, Configuration>() {
        override val name: String = "Cargo Delivery System"
        override suspend fun install(
            context: FeatureInstallContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): CargoDeliverySystem {
            val (extension, intake, rotation, sorting) = Configuration().apply(configure)
            val extensionMotor = context.hardwareMap[DcMotorEx::class, extension].apply {
                direction = DcMotorSimple.Direction.REVERSE
                zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            }
            val intakeMotor = context.hardwareMap[DcMotorEx::class, intake].apply {
                direction = DcMotorSimple.Direction.FORWARD
                zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            }
            val rotationMotor = context.hardwareMap[DcMotorEx::class, rotation].apply {
                direction = DcMotorSimple.Direction.FORWARD
                zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            }
            val sorterServo = context.hardwareMap[ServoImplEx::class, sorting].apply {

            }
            return CargoDeliverySystem(
                extensionMotor,
                rotationMotor,
                intakeMotor,
                sorterServo,
                context.coroutineScope.coroutineContext
            ).apply {
                startPowerUpdates()
            }
        }
    }

    class Configuration : FeatureConfiguration {
        lateinit var extensionMotorName: String
        lateinit var intakeMotorName: String
        lateinit var rotationMotorName: String
        lateinit var sortingServoName: String
        operator fun component1() = extensionMotorName
        operator fun component2() = intakeMotorName
        operator fun component3() = rotationMotorName
        operator fun component4() = sortingServoName
    }

}