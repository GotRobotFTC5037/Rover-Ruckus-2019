package org.firstinspires.ftc.teamcode.lib.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

suspend fun CoroutineScope.cancelAndJoin() {
    this.coroutineContext.cancel()
    this.coroutineContext[Job]!!.join()
}