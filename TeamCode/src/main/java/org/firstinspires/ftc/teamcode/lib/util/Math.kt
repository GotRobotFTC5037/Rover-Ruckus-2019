package org.firstinspires.ftc.teamcode.lib.util

fun Double.clip(range: ClosedFloatingPointRange<Double>) = when {
    this < range.start -> range.start
    this > range.endInclusive -> range.endInclusive
    else -> this
}
