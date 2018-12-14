package org.firstinspires.ftc.teamcode.lib.action

interface MoveActionContext {

    operator fun <E : Element> get(key: Key<E>): E?

    interface Key<E : Element>

    interface Element : MoveActionContext {
        val key: Key<*>
    }
}

object EmptyMoveActionContext : MoveActionContext {
    override fun <E : MoveActionContext.Element> get(key: MoveActionContext.Key<E>): E? = null
}

abstract class AbstractMoveActionContextElement(
    override val key: MoveActionContext.Key<*>
) : MoveActionContext.Element
