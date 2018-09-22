package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.hardware.DcMotor
import java.util.*

abstract class Robot {
    abstract val driveTrain: RobotDriveTrain
}

abstract class RobotComponent

abstract class RobotDriveTrain: RobotComponent() {
    abstract fun move(angle: Double, power: Double)
}

class RobotTankDriveTrain: RobotDriveTrain() {

    private var leftMotors: MutableList<DcMotor> = mutableListOf()
    private var rightMotors: MutableList<DcMotor> = mutableListOf()

    override fun move(angle: Double, power: Double) {
        leftMotors.forEach { it.power = power }
        rightMotors.forEach { it.power = power }
    }
}

class RobotTravelPath {
    val elements: MutableList<RobotTravelPathElement> = mutableListOf()
}

abstract class RobotTravelPathElement {
    abstract fun vectorAt(position: Double): Pair
}


