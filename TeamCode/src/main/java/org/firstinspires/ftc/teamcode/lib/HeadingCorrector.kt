package org.firstinspires.ftc.teamcode.lib

import org.firstinspires.ftc.teamcode.lib.action.MoveActionContext
import org.firstinspires.ftc.teamcode.lib.action.MoveActionScope

interface HeadingCorrector : MoveActionContext.Element {
    fun correctionPower(): Double
    companion object Key : MoveActionContext.Key<HeadingCorrector>
}

object NothingHeadingCorrector : HeadingCorrector {
    override fun correctionPower(): Double = 0.0
}

class WallFollowing : HeadingCorrector {
    override fun correctionPower(): Double {
        TODO("not implemented")
    }
}

fun MoveActionScope.correctionPower() = context[HeadingCorrector].correctionPower()


