package org.firstinspires.ftc.teamcode.lib.util

fun <T> List<T>.sameOrNull(block: (first: T, current: T) -> Boolean): T? {
    val first = this.first()
    for (element in this) {
        if (!block.invoke(first, element)) return null
    }
    return first
}

fun <T> List<T>.sameOrNull(): T? = sameOrNull { first, current -> first == current }