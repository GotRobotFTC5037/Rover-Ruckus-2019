package org.firstinspires.ftc.teamcode.active

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.AnalogInput

@Autonomous
class PotentiometerCode : LinearOpMode() {
    private val potentiometer: AnalogInput by lazy {
        hardwareMap.analogInput.get("potentiometer")
    }

    override fun runOpMode() {
        waitForStart()
        while (true){
            telemetry.addLine("voltage: ${potentiometer.voltage}")
            telemetry.update()
        }
    }


    /*measure the potentiometer input
    add telemetry to see how potentiometer measures and find values
    find and code values for Left, Center, and Right
    put values into sampling code*/
}
