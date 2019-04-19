package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.ServoImplEx
import kotlinx.coroutines.*
import us.gotrobot.grbase.action.ActionScope
import us.gotrobot.grbase.action.action
import us.gotrobot.grbase.action.feature
import us.gotrobot.grbase.feature.*
import us.gotrobot.grbase.robot.RobotContext
import us.gotrobot.grbase.util.get
import kotlin.coroutines.CoroutineContext


class CargoDeliverySystem(
    private val extensionMotor: ManagedMotor,
    private val rotationMotor: ManagedMotor,
    private val intakeMotor: DcMotorEx,
    private val sortingServo: ServoImplEx,
    parentContext: CoroutineContext
) : Feature(), CoroutineScope {

    private val job: Job = Job(parentContext[Job])

    @Suppress("EXPERIMENTAL_API_USAGE")
    override val coroutineContext: CoroutineContext
        get() = CoroutineName("Cargo Delivery System") + job + newSingleThreadContext("Cargo Delivery System")

    enum class SortingDirection {
        LEFT, RIGHT
    }

    private var sortingDirection: SortingDirection = SortingDirection.LEFT

    fun setSortingDirection(direction: SortingDirection) {
        sortingDirection = direction
        sortingServo.position = when (direction) {
            SortingDirection.LEFT -> 0.25
            SortingDirection.RIGHT -> 0.75
        }
    }

    fun setRotationPower(power: Double) {
        rotationMotor.setPower(power)
    }

    suspend fun setRotationPosition(position: Int) {
        rotationMotor.setPosition(position)
    }

    fun setExtensionPower(power: Double) {
        extensionMotor.setPower(power)
    }

    suspend fun setExtendtionPosition(position: Int) {
        extensionMotor.setPosition(position)
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

    companion object Installer : KeyedFeatureInstaller<CargoDeliverySystem, Configuration>() {
        override val name: String = "Cargo Delivery System"
        override suspend fun install(
            context: RobotContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): CargoDeliverySystem {
            val configuration = Configuration().apply(configure)

            val extensionMotor = configuration.extensionMotor
            val rotationMotor = configuration.rotationMotor
            val intakeMotorName = configuration.intakeMotorName
            val sorterServoName = configuration.sortingServoName

            val intakeMotor = context.hardwareMap[DcMotorEx::class, intakeMotorName].apply {
                direction = DcMotorSimple.Direction.REVERSE
                zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            }
            val sorterServo = context.hardwareMap[ServoImplEx::class, sorterServoName].apply {

            }

            return CargoDeliverySystem(
                extensionMotor,
                rotationMotor,
                intakeMotor,
                sorterServo,
                context.coroutineScope.coroutineContext
            )
        }
    }

    class Configuration : FeatureConfiguration {
        lateinit var extensionMotor: ManagedMotor
        lateinit var rotationMotor: ManagedMotor
        lateinit var intakeMotorName: String
        lateinit var sortingServoName: String
    }

}

val ActionScope.cargoDeliverySystem get() = feature(CargoDeliverySystem)
