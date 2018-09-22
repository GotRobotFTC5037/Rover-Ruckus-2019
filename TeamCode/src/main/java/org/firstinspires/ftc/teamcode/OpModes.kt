package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import kotlinx.coroutines.experimental.runBlocking
import org.firstinspires.ftc.teamcode.lib.RobotAction

@Autonomous
class RobotLibraryTest: LinearOpMode() {

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        val robot = TestRobot(this)
        waitForStart()
        runBlocking { robot.runAction(testAction).join() }
    }

    companion object {
        val testAction = RobotAction.customAction {

        }
    }

}

@Autonomous
class TestAuto: LinearOpMode() {
    val leftMotor by lazy {
        hardwareMap.dcMotor.get("left motor").apply {
            direction = DcMotorSimple.Direction.REVERSE
        }
    }

    val rightMotor by lazy {
        hardwareMap.dcMotor.get("right motor")
    }

    override fun runOpMode() {
        leftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        rightMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        leftMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        rightMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        leftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rightMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        waitForStart()

        drive(0.5, 2000)
        sleep(1000)
        drive(-0.5, -400)
        leftMotor.power = 0.3
        rightMotor.power = -0.3
        sleep(1250)
        rightMotor.power = 0.3
        drive(0.5, 5000)
    }

    fun drive(power: Double, distance: Int){
        val initialRightMotorPosition = rightMotor.currentPosition
        val initialLeftMotorPosition = leftMotor.currentPosition
        rightMotor.power = power
        leftMotor.power = power
        when {
            distance > 0 -> while (
                    leftMotor.currentPosition < initialLeftMotorPosition + distance &&
                    rightMotor.currentPosition < initialRightMotorPosition + distance &&
                    opModeIsActive()
            ){
                idle()
            }
            distance < 0 -> while (
                    leftMotor.currentPosition > initialLeftMotorPosition + distance &&
                    rightMotor.currentPosition > initialRightMotorPosition + distance &&
                    opModeIsActive()
            ){
                idle()
            }
            else -> return
        }

        leftMotor.power = 0.0
        rightMotor.power = 0.0
    }
}
