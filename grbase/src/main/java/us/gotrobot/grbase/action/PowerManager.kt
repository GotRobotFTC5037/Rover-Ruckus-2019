package us.gotrobot.grbase.action

import kotlin.math.sign

interface PowerManager : MoveActionContext.Element {
    var target: Double
    suspend fun power(): Double

    companion object Key : MoveActionContext.Key<PowerManager>
}

class NothingPowerManager : PowerManager {
    override var target: Double = 0.0
    override suspend fun power(): Double = 0.0
}

class ConstantPowerManager(private val power: Double) : PowerManager {
    override var target: Double = 0.0
    override suspend fun power(): Double = power * sign(target)
}

class EasingPowerManager() : PowerManager {
    override var target: Double = TODO()

    override suspend fun power(): Double {
        return 0.0
    }
}

class MotionProfilePowerManager(
    private val maximumSpeed: Double,
    private val maximumAcceleration: Double
) : PowerManager {

    private var motionProfile: MotionProfile = MotionProfile.Empty

    override var target: Double
        get() = motionProfile.endState.position
        set(value) {
            motionProfile = MotionProfile.generate(value, maximumSpeed, maximumAcceleration)
        }

    override suspend fun power(): Double {
        TODO()
    }
}

var MoveActionScope.target: Double
    get() = context[PowerManager].target
    set(value) {
        context[PowerManager].target = value
    }

suspend fun MoveActionScope.power() = context[PowerManager].power()

infix fun MoveAction.with(manger: PowerManager): MoveAction {
    context[PowerManager] = manger
    return this
}

fun power(power: Double): PowerManager = ConstantPowerManager(power)