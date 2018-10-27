package org.firstinspires.ftc.teamcode.lib.opencv

import org.opencv.core.*
import org.opencv.imgproc.Imgproc

fun Mat.resized(width: Int, height: Int): Mat {
    val output = Mat()
    Imgproc.resize(
        this, output,
        Size(width.toDouble(), height.toDouble()),
        0.0, 0.0,
        Imgproc.INTER_NEAREST
    )
    return output
}

fun Mat.boxBlurred(radius: Double): Mat {
    val kernelSize = 2 * radius + 1
    val output = Mat()
    Imgproc.blur(this, output, Size(kernelSize, kernelSize))
    return output
}

fun Mat.hsvThresholding(
    hue: ClosedFloatingPointRange<Double>,
    saturation: ClosedFloatingPointRange<Double>,
    value: ClosedFloatingPointRange<Double>
): Mat {
    val output = Mat()
    Imgproc.cvtColor(this, output, Imgproc.COLOR_BGR2HSV)
    Core.inRange(
        output,
        Scalar(hue.start, saturation.start, value.start),
        Scalar(hue.endInclusive, saturation.endInclusive, value.endInclusive),
        output
    )
    return output
}

fun Mat.findContours(externalOnly: Boolean = true): MutableList<MatOfPoint> {
    val hierarchy = Mat()
    val contours = mutableListOf<MatOfPoint>()
    val mode = if (externalOnly) {
        Imgproc.RETR_EXTERNAL
    } else {
        Imgproc.RETR_LIST
    }
    val method = Imgproc.CHAIN_APPROX_SIMPLE
    Imgproc.findContours(this, contours, hierarchy, mode, method)
    return contours
}

fun Mat.dilated(iterations: Int): Mat {
    val output = Mat()
    Imgproc.dilate(
        this,
        output,
        Mat(),
        Point(-1.0, -1.0),
        iterations,
        Core.BORDER_CONSTANT,
        Scalar(-1.0)
    )
    return output
}