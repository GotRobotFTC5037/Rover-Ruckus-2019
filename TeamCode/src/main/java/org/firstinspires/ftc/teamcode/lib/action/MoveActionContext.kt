package org.firstinspires.ftc.teamcode.lib.action

class MoveActionContext {

    private val elements = mutableMapOf<Key<*>, Element>()

    operator fun contains(key: Key<*>): Boolean = elements.contains(key)

    operator fun <T : Element> set(key: Key<T>, value: T) {
        elements[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Element> get(key: Key<T>): T = elements[key] as T

    interface Key<T : Element>

    interface Element

}
