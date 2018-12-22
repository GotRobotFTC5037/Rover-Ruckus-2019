package org.firstinspires.ftc.teamcode.lib

import org.firstinspires.ftc.teamcode.lib.action.MoveActionContext
import org.firstinspires.ftc.teamcode.lib.action.MoveActionScope

interface PowerManager : MoveActionContext.Element {
    fun calculatePower(): Double
    companion object Key : MoveActionContext.Key<PowerManager>
}

object NothingPowerManager : PowerManager {
    override fun calculatePower(): Double = 0.0
}

class ConstantPowerManager(private val power: Double) : PowerManager {
    override fun calculatePower(): Double = power
}

fun MoveActionScope.power() = context[PowerManager].calculatePower()