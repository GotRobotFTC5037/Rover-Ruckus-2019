package org.firstinspires.ftc.teamcode.lib.action

interface PowerManager : MoveActionContext.Element {

    suspend fun power(): Double

    companion object Key : MoveActionContext.Key<PowerManager>

}

class NothingPowerManager : PowerManager {

    override suspend fun power(): Double = 0.0

}

class ConstantPowerManager(private val power: Double) : PowerManager {

    override suspend fun power(): Double = power

}

class EaseingPowerManager() : PowerManager {

    override suspend fun power(): Double {
        TODO()
    }

}

suspend fun MoveActionScope.power() = context[PowerManager].power()