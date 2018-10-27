package org.firstinspires.ftc.teamcode.lib.opencv

import org.opencv.core.*
import org.opencv.imgproc.Imgproc

@Suppress("ArrayInDataClass")
data class ContourFilter(
    var minArea: Double = 0.0,
    var minPerimeter: Double = 0.0,
    var minWidth: Double = 0.0,
    var maxWidth: Double = Double.MAX_VALUE,
    var minHeight: Double = 0.0,
    var maxHeight: Double = Double.MAX_VALUE,
    var solidity: ClosedFloatingPointRange<Double> = 0.0..100.0,
    var maxVertexCount: Double = Double.MAX_VALUE,
    var minVertexCount: Double = 0.0,
    var minRatio: Double = 0.0,
    var maxRatio: Double = Double.MAX_VALUE
)

fun List<MatOfPoint>.filtered(
    minArea: Double = 0.0,
    minPerimeter: Double = 0.0,
    minWidth: Double = 0.0,
    maxWidth: Double = Double.MAX_VALUE,
    minHeight: Double = 0.0,
    maxHeight: Double = Double.MAX_VALUE,
    solidity: ClosedFloatingPointRange<Double> = 0.0..100.0,
    maxVertexCount: Double = Double.MAX_VALUE,
    minVertexCount: Double = 0.0,
    minRatio: Double = 0.0,
    maxRatio: Double = Double.MAX_VALUE
): List<MatOfPoint> {
    val hull = MatOfInt()
    val output = mutableListOf<MatOfPoint>()
    for (i in this.indices) {
        val contour = this[i]
        val bb = Imgproc.boundingRect(contour)
        if (bb.width < minWidth || bb.width > maxWidth) continue
        if (bb.height < minHeight || bb.height > maxHeight) continue
        val area = Imgproc.contourArea(contour)
        if (area < minArea) continue
        if (Imgproc.arcLength(MatOfPoint2f(*contour.toArray()), true) < minPerimeter) continue
        Imgproc.convexHull(contour, hull)
        val mopHull = MatOfPoint()
        mopHull.create(hull.size().height.toInt(), 1, CvType.CV_32SC2)
        var j = 0
        while (j < hull.size().height) {
            val index = hull.get(j, 0)[0].toInt()
            val point = doubleArrayOf(contour.get(index, 0)[0], contour.get(index, 0)[1])
            mopHull.put(j, 0, *point)
            j++
        }
        val solid = 100 * area / Imgproc.contourArea(mopHull)
        if (solid < solidity.start || solid > solidity.endInclusive) continue
        if (contour.rows() < minVertexCount || contour.rows() > maxVertexCount) continue
        val ratio = bb.width / bb.height.toDouble()
        if (ratio < minRatio || ratio > maxVertexCount) continue
        output.add(contour)
    }
    return output
}

fun List<MatOfPoint>.convexHulls(): List<MatOfPoint> {
    val hull = MatOfInt()
    val outputContours = mutableListOf<MatOfPoint>()
    for (i in this.indices) {
        val contour = this[i]
        val mopHull = MatOfPoint()
        Imgproc.convexHull(contour, hull)
        mopHull.create(hull.size().height.toInt(), 1, CvType.CV_32SC2)
        var j = 0
        while (j < hull.size().height) {
            val index = hull.get(j, 0)[0].toInt()
            val point = doubleArrayOf(contour.get(index, 0)[0], contour.get(index, 0)[1])
            mopHull.put(j, 0, *point)
            j++
        }
        outputContours.add(mopHull)
    }
    return outputContours
}

fun List<MatOfPoint>.boundingBoxes(): List<Rect> {
    return this.map { Imgproc.boundingRect(it) }
}

fun MatOfPoint.boundingBox(): Rect {
    return Imgproc.boundingRect(this)
}