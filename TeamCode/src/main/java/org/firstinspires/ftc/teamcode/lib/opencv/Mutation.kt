package org.firstinspires.ftc.teamcode.lib.opencv

import org.opencv.core.*
import org.opencv.imgproc.Imgproc

data class RGBColor(
    val red: Double = 0.0,
    val blue: Double = 0.0,
    val green: Double = 0.0
) {
    companion object {
        val red = RGBColor(red = 255.0)
        val blue = RGBColor(blue = 255.0)
        val green = RGBColor(green = 255.0)
    }
}

fun addBoundingBoxAndText(
    mat: Mat,
    x: Double,
    y: Double,
    size: Double,
    text: String,
    color: RGBColor,
    borderWidth: Int = 1
) {
    val radius = size / 2
    val colorScalar = Scalar(color.red, color.blue, color.green)
    Imgproc.rectangle(
        mat,
        Point(x - radius, y - radius),
        Point(x + radius, y + radius),
        colorScalar,
        borderWidth
    )
    Imgproc.putText(
        mat,
        text,
        Point(x - radius, y - radius - 5),
        0,
        0.5,
        colorScalar,
        2
    )
}

fun Mat.addBoundingBoxAndText(blob: KeyPoint, text: String, color: RGBColor, borderWidth: Int = 1) {
    addBoundingBoxAndText(
        this,
        blob.pt.x, blob.pt.y, blob.size.toDouble(),
        text, color, borderWidth
    )
}

fun Mat.addBoundingBoxAndText(
    contour: MatOfPoint,
    text: String,
    color: RGBColor,
    borderWidth: Int = 1
) {
    val boundingRectangle = Imgproc.boundingRect(contour)
    addBoundingBoxAndText(
        this,
        boundingRectangle.x.toDouble(),
        boundingRectangle.y.toDouble(),
        (boundingRectangle.width + boundingRectangle.height) / 2.0,
        text, color, borderWidth
    )
}

fun Mat.addBoundingBoxAndText(
    rect: Rect, text: String, color: RGBColor, borderWidth: Int = 1
) {
    addBoundingBoxAndText(
        this,
        rect.x.toDouble() + rect.width / 2,
        rect.y.toDouble() + rect.width / 2,
        (rect.width + rect.height) / 2.0,
        text, color, borderWidth
    )
}
