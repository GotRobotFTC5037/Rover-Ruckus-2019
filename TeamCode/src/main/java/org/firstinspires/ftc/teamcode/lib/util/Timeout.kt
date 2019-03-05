//package org.firstinspires.ftc.teamcode.lib.util
//
//import org.firstinspires.ftc.teamcode.lib.action.Action
//
//@Suppress("EXPERIMENTAL_FEATURE_WARNING")
//inline class Timeout(val duration: Long)
//
//infix fun Action.with(timeout: Timeout): Action {
//    this.timeoutMillis = timeout.duration
//    return this
//}