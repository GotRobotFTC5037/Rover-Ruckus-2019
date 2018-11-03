package org.firstinspires.ftc.teamcode.lib

import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector

fun objectDetector(
    parameters: TFObjectDetector.Parameters,
    localizer: VuforiaLocalizer
): TFObjectDetector =
    ClassFactory.getInstance().createTFObjectDetector(parameters, localizer)

@Suppress("FunctionName")
fun VuforiaLocalizer(parameters: VuforiaLocalizer.Parameters): VuforiaLocalizer =
    ClassFactory.getInstance().createVuforia(parameters)

