package org.firstinspires.ftc.teamcode.lib.action

import org.firstinspires.ftc.teamcode.lib.HeadingCorrector
import org.firstinspires.ftc.teamcode.lib.NothingHeadingCorrector
import org.firstinspires.ftc.teamcode.lib.NothingPowerManager
import org.firstinspires.ftc.teamcode.lib.PowerManager

class MoveActionContext(val type: MoveActionType) {

    private val elements = mutableMapOf<Key<*>, Element>()

    init {
        elements[PowerManager] = NothingPowerManager
        elements[HeadingCorrector] = NothingHeadingCorrector
    }

    operator fun contains(key: Key<*>): Boolean = elements.contains(key)

    operator fun <T : Element> set(key: Key<T>, value: T) {
        elements[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Element> get(key: Key<T>): T = elements[key] as T

    interface Key<T : Element>

    interface Element
}
