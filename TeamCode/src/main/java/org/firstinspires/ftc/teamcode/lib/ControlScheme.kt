package org.firstinspires.ftc.teamcode.lib

import com.qualcomm.robotcore.hardware.Gamepad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.firstinspires.ftc.teamcode.lib.action.ActionScope
import org.firstinspires.ftc.teamcode.lib.util.delayUntilStop
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty0

class RobotGamepad(gamepad: Gamepad) {
    val a = RobotGamepadButton(gamepad::a)
    val b = RobotGamepadButton(gamepad::b)
    val x = RobotGamepadButton(gamepad::x)
    val y = RobotGamepadButton(gamepad::y)
    val back = RobotGamepadButton(gamepad::back)
    val start = RobotGamepadButton(gamepad::start)
    val leftBumper = RobotGamepadButton(gamepad::left_bumper)
    val rightBumper = RobotGamepadButton(gamepad::right_bumper)
    val leftTrigger = RobotGamepadTrigger(gamepad::left_trigger)
    val rightTrigger = RobotGamepadTrigger(gamepad::right_trigger)
}

data class RobotGamepadJoystickUpdate(
    val leftX: Double,
    val leftY: Double,
    val rightX: Double,
    val rightY: Double
)

class RobotGamepadButton(
    private val property: KProperty0<Boolean>
) {
    val isPressed: Boolean get() = property.get()
}

class RobotGamepadTrigger(
    private val property: KProperty0<Float>
) {
    val value: Double get() = property.get().toDouble()
}

class ControlScheme(
    gamepad1: Gamepad,
    gamepad2: Gamepad
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default

    val driver: RobotGamepad = RobotGamepad(gamepad1)
    val gunner: RobotGamepad = RobotGamepad(gamepad2)

}

suspend inline fun ActionScope.driverControl(crossinline block: suspend ControlScheme.() -> Unit) {
    val controlScheme = TODO()
    block.invoke(controlScheme)
    robot.linearOpMode.delayUntilStop()
}