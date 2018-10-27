package org.firstinspires.ftc.teamcode.lib.opencv

import org.opencv.core.KeyPoint
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.FastFeatureDetector
import java.io.File
import java.io.FileWriter
import java.io.IOException

fun Mat.findBlobs(
    minArea: Double = 0.0,
    circularity: ClosedFloatingPointRange<Double> = 0.0..100.0,
    darkBlobs: Boolean = false
): List<KeyPoint> {
    val blobDet = FastFeatureDetector.create(FastFeatureDetector.TYPE_9_16)
    try {
        val tempFile = File.createTempFile("config", ".xml")
        val configuration =
            """
                    <?xml version=\"1.0\"?>\n
                    <opencv_storage>\n
                    <thresholdStep>10.</thresholdStep>\n
                    <minThreshold>50.</minThreshold>\n
                    <maxThreshold>220.</maxThreshold>\n
                    <minRepeatability>2</minRepeatability>\n
                    <minDistBetweenBlobs>10.</minDistBetweenBlobs>\n
                    <filterByColor>1</filterByColor>\n
                    <blobColor>
                    ${if (darkBlobs) 0 else 255}
                    </blobColor>\n
                    <filterByArea>1</filterByArea>\n
                    <minArea>
                    $minArea
                    </minArea>\n
                    <maxArea>
                    ${Integer.MAX_VALUE}
                    </maxArea>\n
                    <filterByCircularity>1</filterByCircularity>\n
                    <minCircularity>
                    ${circularity.start}
                    </minCircularity>\n
                    <maxCircularity>
                    ${circularity.endInclusive}
                    </maxCircularity>\n
                    <filterByInertia>1</filterByInertia>\n
                    <minInertiaRatio>0.1</minInertiaRatio>\n
                    <maxInertiaRatio>${Integer.MAX_VALUE}</maxInertiaRatio>\n
                    <filterByConvexity>1</filterByConvexity>\n
                    <minConvexity>0.95</minConvexity>\n
                    <maxConvexity>${Integer.MAX_VALUE}</maxConvexity>\n
                    </opencv_storage>\n
                """.trimIndent()
        val writer = FileWriter(tempFile, false)
        writer.write(configuration)
        writer.close()
        blobDet.read(tempFile.path)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    val output = MatOfKeyPoint()
    blobDet.detect(this, output)
    return output.toList()
}