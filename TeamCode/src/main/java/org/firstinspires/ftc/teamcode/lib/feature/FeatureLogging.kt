package org.firstinspires.ftc.teamcode.lib.feature

import org.firstinspires.ftc.robotcore.external.Telemetry

class FeatureTelemetry(private val telemetry: Telemetry, private val featureName: String) {

    val log = telemetry.log()

    fun logEvent(data: String) {
        log.add("[$featureName] $data")
    }

}