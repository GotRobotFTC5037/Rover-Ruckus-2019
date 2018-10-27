package org.firstinspires.ftc.teamcode.lib.action

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.Gamepad
import kotlinx.coroutines.isActive
import org.firstinspires.ftc.teamcode.lib.robot.Robot
import kotlin.coroutines.CoroutineContext

private class TeleOpAction(private val block: suspend TeleOpActionScope.() -> Unit) : Action() {

    override var name = "TeleOp"

    override suspend fun run(robot: Robot, parentContext: CoroutineContext) {
        val scope = TeleOpActionScope(robot, parentContext)
        block.invoke(scope)
        with(scope) {
            while (isActive) {
                controlScheme.runElements()
            }
        }
    }

}

fun teleOp(block: suspend TeleOpActionScope.() -> Unit): Action = TeleOpAction(block)

fun Robot.performTeleOp(block: suspend TeleOpActionScope.() -> Unit): Unit = perform(teleOp(block))

class TeleOpActionScope(
    private val robot: Robot,
    parentContext: CoroutineContext
) : StandardActionScope(robot, parentContext) {

    internal lateinit var controlScheme: ControlScheme

    fun controlScheme(configure: ControlScheme.Configuration.() -> Unit) {
        val controlScheme = ControlScheme(robot.linearOpMode)
        controlScheme.Configuration().apply(configure)
        this.controlScheme = controlScheme
    }

}

class ControlScheme(private val linearOpMode: LinearOpMode) {

    private val elements: MutableList<() -> Unit> = mutableListOf()

    fun runElements(): Unit = elements.forEach { it.invoke() }

    inner class Configuration {

        val gamepad1: ControlSchemeGamepad = ControlSchemeGamepad(linearOpMode.gamepad1)
        val gamepad2: ControlSchemeGamepad = ControlSchemeGamepad(linearOpMode.gamepad2)

        fun ControlSchemeGamepad.joysticks(block: JoystickPositions.() -> Unit) {
            elements.add {
                val joystickPositions = JoystickPositions(
                    -gamepad.left_stick_y.toDouble(),
                    -gamepad.right_stick_y.toDouble(),
                    gamepad.left_stick_x.toDouble(),
                    gamepad.right_stick_x.toDouble()
                )
                block.invoke(joystickPositions)
            }
        }

    }

}

data class JoystickPositions(
    val leftY: Double,
    val rightY: Double,
    val leftX: Double,
    val rightX: Double
)

class ControlSchemeGamepad(val gamepad: Gamepad)



