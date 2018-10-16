package org.firstinspires.ftc.teamcode.lib.util

import kotlinx.coroutines.experimental.yield
import kotlin.reflect.KProperty0

suspend inline fun waitFor(property: KProperty0<Boolean>) {
    while (!property.get()) {
        yield()
    }
}

suspend inline fun waitFor(predicate: () -> Boolean) {
    while (!predicate()) {
        yield()
    }
}
