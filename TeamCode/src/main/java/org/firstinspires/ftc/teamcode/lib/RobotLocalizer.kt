package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference

data class RobotLocation (
    val heading: Double
)

interface RobotLocalizer {
    val locationProducer: ReceiveChannel<RobotLocation>
    fun setup(linearOpMode: LinearOpMode, coroutineScope: CoroutineScope)
}

class IMULocalizer(val name: String = "imu"): RobotLocalizer {

    lateinit var linearOpMode: LinearOpMode
    lateinit var coroutineScope: CoroutineScope

    private lateinit var imu: BNO055IMU

    private fun BNO055IMU.waitForGyroCalibration() {
        while (isGyroCalibrated.not() && linearOpMode.isStopRequested.not()) {
            linearOpMode.idle()
        }
    }

    override fun setup(linearOpMode: LinearOpMode, coroutineScope: CoroutineScope) {
        this.linearOpMode = linearOpMode
        this.coroutineScope = coroutineScope
        imu = linearOpMode.hardwareMap.get(BNO055IMU::class.java, "imu").apply {
            initialize(BNO055IMU.Parameters().apply {
                angleUnit = BNO055IMU.AngleUnit.DEGREES
            })
        }
        imu.waitForGyroCalibration()
    }

    private fun CoroutineScope.produceLocation(): ReceiveChannel<RobotLocation> = produce {
        while(linearOpMode.isStopRequested.not()) {
            val orientation = imu.getAngularOrientation(
                AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES
            )
            val location = RobotLocation(orientation.firstAngle.toDouble())
            send(location)
        }
    }

    override val locationProducer: ReceiveChannel<RobotLocation>
        get() = coroutineScope.produceLocation()
}