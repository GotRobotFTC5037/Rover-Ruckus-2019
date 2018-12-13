package org.firstinspires.ftc.teamcode.lib.action

import kotlinx.coroutines.withTimeout
import org.firstinspires.ftc.teamcode.lib.NothingPowerManager
import org.firstinspires.ftc.teamcode.lib.PowerManager
import org.firstinspires.ftc.teamcode.lib.PowerManagerScope
import org.firstinspires.ftc.teamcode.lib.feature.DriveTrain
import org.firstinspires.ftc.teamcode.lib.feature.RobotHeadingLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.RobotPositionLocalizer
import org.firstinspires.ftc.teamcode.lib.feature.TankDriveTrain
import org.firstinspires.ftc.teamcode.lib.robot.Robot

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
    val driveTrain = requestFeature(DriveTrain::class)
    val localizer = requestFeature(RobotPositionLocalizer::class)

    val positionChannel = localizer.position.openSubscription()
    val initialPosition = positionChannel.receive().linearPosition
    val targetPosition = initialPosition + deltaDistance

    when (driveTrain) {
        is TankDriveTrain -> when {
            deltaDistance > 0 -> while (true) {
                val position = positionChannel.receive().linearPosition
                if (position >= targetPosition) break
                notifyProgress(position)
                driveTrain.setMotorPowers(power(), power())
            }
            deltaDistance < 0 -> while (true) {
                val position = positionChannel.receive().linearPosition
                if (position <= targetPosition) break
                notifyProgress(position)
                driveTrain.setMotorPowers(-power(), -power())
            }
        }
        else -> TODO()
    }

    positionChannel.cancel()
    driveTrain.stop()
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

    val driveTrain = requestFeature(DriveTrain::class)
    val localizer = requestFeature(RobotHeadingLocalizer::class)

    val headingChannel = localizer.heading.openSubscription()
    val initialHeading = headingChannel.receive()
    val adjustedTargetHeading = properHeading(targetHeading)

    when (driveTrain) {
        is TankDriveTrain -> when {
            initialHeading > adjustedTargetHeading -> while (true) {
                val heading = headingChannel.receive()
                if (heading <= adjustedTargetHeading) break
                notifyProgress(heading)
                driveTrain.setMotorPowers(power(), -power())
            }
            initialHeading < adjustedTargetHeading -> while (true) {
                val heading = headingChannel.receive()
                if (heading >= adjustedTargetHeading) break
                notifyProgress(heading)
                driveTrain.setMotorPowers(-power(), power())
            }
        }
        else -> TODO()
    }

    headingChannel.cancel()
    driveTrain.stop()
}.apply {
    attributes[MoveActionKeys.TargetHeading] = targetHeading
    attributes[MoveActionKeys.Type] = MoveActionType.TURN

}
