package org.firstinspires.ftc.teamcode.lib.opmode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

abstract class CoroutineOpMode : OpMode(), OpmodeStatus,  CoroutineScope {

    private lateinit var job: Job

    private var throwable: Throwable = NullThrowable

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        this.throwable = throwable
    }

    final override val coroutineContext: CoroutineContext
        get() = CoroutineName("OpMode") + Dispatchers.Default + job + exceptionHandler

    override var isInitialized: Boolean = false

    override var isStarted: Boolean = false

    override var isStopped: Boolean = false

    private fun handleLoop() {
        if (throwable != NullThrowable) {
            when (throwable) {
                is RuntimeException -> throw throwable
                else -> throw RuntimeException(throwable)
            }
        }
    }

    abstract suspend fun initialize()

    abstract suspend fun run()

    private suspend fun waitForStart() {
        while (!isStarted && isActive) {
            yield()
        }
    }

    final override fun init() {
        telemetry.log().add("[OpMode] Starting initialization")
        isInitialized = true
        job = Job()
        launch {
            val time = measureTimeMillis { initialize() }
            telemetry.log().add("[OpMode] Done initialization (${time}ms)")
            waitForStart()
            telemetry.log().add("[OpMode] Starting opmode")
            run()
            requestOpModeStop()
        }
    }

    final override fun init_loop() {
        handleLoop()
    }

    final override fun start() {
        isStarted = true
    }

    final override fun loop() {
        handleLoop()
    }

    final override fun stop() {
        telemetry.log().add("[OpMode] Stopping opmode")
        isStopped = true
        runBlocking { job.cancelAndJoin() }
    }

}

object NullThrowable : Throwable()
