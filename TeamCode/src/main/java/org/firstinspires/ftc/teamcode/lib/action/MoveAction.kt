package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.NothingPowerManager
import org.firstinspires.ftc.teamcode.lib.PowerManager
import org.firstinspires.ftc.teamcode.lib.PowerManagerScope
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.DriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.drivetrain.TankDriveTrainLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.localizer.IMULocalizer
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.math.abs

/**
 * Provides an action block for a [Robot] to run and provided context specifically for moving the
 * robot.
 */
open class MoveAction(
    private val block: suspend MoveActionScope.() -> Unit
) : AbstractAction() {

    val attributes: MoveActionAttributes = MoveActionAttributes()

    override suspend fun run(robot: Robot) {
        if (!disabled) {
            withTimeout(timeoutMillis) {
                MoveActionScope(robot, attributes[MoveActionKeys.PowerManager]).also {
                    block.invoke(it)
                }
            }
        }
    }

}

/**
 * That [ActionScope] that is used in the scope of a [MoveAction] block.
 */
class MoveActionScope(
    robot: Robot,
    private val powerManager: PowerManager
) : AbstractActionScope(robot), PowerManagerScope {

    private var initialValue: Double = 0.0
    private var targetValue: Double = 0.0
    private var progress: Double = 0.0

    override fun setRange(initialValue: Double, targetValue: Double) {
        this.initialValue = initialValue
        this.targetValue = targetValue
    }

    override fun notifyProgress(progress: Double) {
        this.progress = progress
    }

    override fun power(): Double = powerManager.calculatePower(initialValue, targetValue, progress)

}

class MoveActionAttributes {

    private val attributes = mutableMapOf<MoveActionKey<*>, Any?>()

    init {
        set(MoveActionKeys.PowerManager, NothingPowerManager)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: MoveActionKey<T>): T {
        return attributes[key] as T
    }

    operator fun <T> set(key: MoveActionKey<T>, value: T) {
        attributes[key] = value
    }

}

@Suppress("unused")
class MoveActionKey<T>

object MoveActionKeys {
    val PowerManager = MoveActionKey<PowerManager>()
    val Duration = MoveActionKey<Long>()
    val Distance = MoveActionKey<Double>()
    val TargetHeading = MoveActionKey<Double>()
    val Type = MoveActionKey<MoveActionType>()
}

enum class MoveActionType {
    DRIVE, TURN
}

fun move(block: suspend MoveActionScope.() -> Unit): MoveAction = MoveAction(block)

@Deprecated("Usage of time based drives should be avoided.")
fun timeDrive(duration: Long): MoveAction = move {
    TODO()
}.apply {
    attributes[MoveActionKeys.Duration] = duration
    attributes[MoveActionKeys.Type] = MoveActionType.DRIVE
}

@Deprecated("Usage of time turns drives should be avoided.")
fun timeTurn(duration: Long): MoveAction = move {
    TODO()
}.apply {
    attributes[MoveActionKeys.Duration] = duration
    attributes[MoveActionKeys.Type] = MoveActionType.TURN
}

fun drive(deltaDistance: Double): MoveAction = move {
    when (val driveTrain = requestFeature(DriveTrain::class)) {
        is TankDriveTrain -> {
            val localizer = requestFeature(TankDriveTrainLocalizer)
            val positionChannel = localizer.newPositionChannel()

            val driveTrainJob = launch {
                if (deltaDistance > 0) {
                    while (true) {
                        driveTrain.setMotorPowers(power(), power())
                        yield()
                    }
                } else if (deltaDistance < 0) {
                    while (true) {
                        driveTrain.setMotorPowers(-power(), -power())
                        yield()
                    }
                }
            }.apply {
                invokeOnCompletion {
                    driveTrain.stop()
                }
            }

            for (update in positionChannel) {
                if (abs(update.average) > abs(deltaDistance)) {
                    positionChannel.cancel()
                }
                yield()
            }

            driveTrainJob.cancel()
        }

        else -> TODO()
    }
}.apply {
    attributes[MoveActionKeys.Distance] = deltaDistance
    attributes[MoveActionKeys.Type] = MoveActionType.DRIVE
}

fun turnTo(targetHeading: Double): MoveAction = move {

    tailrec fun properHeading(heading: Double): Double = when {
        heading > 180 -> properHeading(heading - 360)
        heading < -180 -> properHeading(heading + 360)
        else -> heading
    }

    when (val driveTrain = requestFeature(DriveTrain::class)) {
        is TankDriveTrain -> {
            val localizer = requestFeature(IMULocalizer)
            val headingChannel = localizer.newHeadingChannel()

            val adjustedTargetHeading = properHeading(targetHeading)
            val initialHeading = properHeading(headingChannel.receive())

            val driveTrainJob = launch {
                if (adjustedTargetHeading < initialHeading) {
                    while (true) {
                        driveTrain.setMotorPowers(-power(), power())
                        yield()
                    }
                } else if (adjustedTargetHeading > initialHeading) {
                    while (true) {
                        driveTrain.setMotorPowers(power(), -power())
                        yield()
                    }
                }
            }.apply {
                invokeOnCompletion {
                    driveTrain.stop()
                }
            }

            if (adjustedTargetHeading > initialHeading) {
                headingChannel.first {
                    telemetry.addLine()
                        .addData("Target", adjustedTargetHeading)
                        .addData("Heading", it)
                    telemetry.update()
                    adjustedTargetHeading <= it
                }
            } else if (adjustedTargetHeading < initialHeading) {
                headingChannel.first {
                    telemetry.addLine()
                        .addData("Target", adjustedTargetHeading)
                        .addData("Heading", it)
                    telemetry.update()
                    adjustedTargetHeading >= it
                }
            }

            driveTrainJob.cancel()
            driveTrain.stop()
        }

        else -> TODO()
    }

}.apply {
    attributes[MoveActionKeys.TargetHeading] = targetHeading
    attributes[MoveActionKeys.Type] = MoveActionType.TURN
}
