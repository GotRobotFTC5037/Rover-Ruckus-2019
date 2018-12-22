package org.firstinspires.ftc.teamcode.lib.action

interface MoveActionType

interface MoveActionClause

object UnspecifiedMoveActionType : MoveActionType, MoveActionClause

class Drive(val distance: Double) : MoveActionType {
    companion object : MoveActionClause
}

class TurnTo(val targetHeading: Double) : MoveActionType {
    companion object : MoveActionClause
}
