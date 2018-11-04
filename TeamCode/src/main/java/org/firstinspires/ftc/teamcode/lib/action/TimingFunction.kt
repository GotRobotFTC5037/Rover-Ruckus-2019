package org.firstinspires.ftc.teamcode.lib.action

sealed class TimingFunction {
    abstract fun valueAt(t: Double): Double
}

object LinearTimingFunction : TimingFunction() {
    override fun valueAt(t: Double): Double {
        return t
    }
}

class EasingTimingFunction(private val outputRange: ClosedFloatingPointRange<Double>) :
    TimingFunction() {
    override fun valueAt(t: Double): Double =
        outputRange.start + (outputRange.endInclusive - outputRange.start) * if (t < 0.5) 4 * t else 4 * (1 - t)
}