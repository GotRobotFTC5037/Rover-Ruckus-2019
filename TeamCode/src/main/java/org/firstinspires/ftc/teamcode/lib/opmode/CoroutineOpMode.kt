package org.firstinspires.ftc.teamcode.lib.opmode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import kotlinx.coroutines.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.internal.opmode.TelemetryImpl
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

abstract class CoroutineOpMode : OpMode(), CoroutineScope {

    private lateinit var job: Job

    private var throwable: Throwable = NullThrowable

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        this.throwable = throwable
    }

    final override val coroutineContext: CoroutineContext
        get() = CoroutineName("OpMode") + Dispatchers.Default + job + exceptionHandler

    private lateinit var initializeJob: Job

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

    final override fun init() {
        telemetry.log().add("[OpMode] Starting initialization")
        job = Job()
        initializeJob = launch {
            val time = measureTimeMillis {
                initialize()
            }
            telemetry.log().add("[OpMode] Done initialization (${time}ms)")
        }
    }

    final override fun init_loop() {
        handleLoop()
    }

    final override fun start() {
        launch {
            initializeJob.join()
            telemetry.log().add("[OpMode] Starting opmode")
            run()
        }
    }

    final override fun loop() {
        handleLoop()
    }

    final override fun stop() {
        job.cancel()
    }

}

object NullThrowable : Throwable()
