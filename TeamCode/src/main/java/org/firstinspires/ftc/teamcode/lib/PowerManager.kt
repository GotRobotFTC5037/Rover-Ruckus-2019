package org.firstinspires.ftc.teamcode.lib

import org.firstinspires.ftc.teamcode.lib.util.clip

interface PowerManagerScope {
    fun setRange(initialValue: Double, targetValue: Double)
    fun notifyProgress(progress: Double)
    fun power(): Double
}

interface PowerManager {
    fun calculatePower(initialValue: Double, targetValue: Double, progress: Double): Double
}

object NothingPowerManager : PowerManager {
    override fun calculatePower(
        initialValue: Double,
        targetValue: Double,
        progress: Double
    ): Double {
        return 0.0
    }
}

class ConstantPowerManager(private val power: Double) :
    PowerManager {
    override fun calculatePower(
        initialValue: Double,
        targetValue: Double,
        progress: Double
    ): Double {
        return power
    }
}

class EasingPowerManager(
    private val powerRange: ClosedFloatingPointRange<Double>,
    private val powerRampSlope: Double
) : PowerManager {

    override fun calculatePower(
        initialValue: Double,
        targetValue: Double,
        progress: Double
    ): Double {
        val completionPercentage = progress / (targetValue - initialValue)
        return if (completionPercentage < 0.5) {
            (powerRange.start + (progress - initialValue) * powerRampSlope).clip(powerRange)
        } else {
            (powerRange.start + (1 - progress - initialValue) * powerRampSlope).clip(powerRange)
        }
    }

}