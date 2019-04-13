package us.gotrobot.grbase.drivercontrol

import com.qualcomm.robotcore.hardware.Gamepad
import us.gotrobot.grbase.action.feature
import us.gotrobot.grbase.action.perform
import us.gotrobot.grbase.feature.Feature
import us.gotrobot.grbase.feature.FeatureConfiguration
import us.gotrobot.grbase.feature.FeatureSet
import us.gotrobot.grbase.feature.KeyedFeatureInstaller
import us.gotrobot.grbase.robot.FeatureInstallContext
import us.gotrobot.grbase.robot.Robot
import kotlin.reflect.KProperty0

class DriverControl(val driver: Gamepad, val gunner: Gamepad) : Feature() {

    operator fun component1() = driver
    operator fun component2() = gunner

    companion object Installer : KeyedFeatureInstaller<DriverControl, Configuration>() {

        override val name: String = "Driver Control"

        override suspend fun install(
            context: FeatureInstallContext,
            featureSet: FeatureSet,
            configure: Configuration.() -> Unit
        ): DriverControl {
            val (g0, g1) = context.gamepads
            return DriverControl(g0, g1)
        }
    }

    class Configuration : FeatureConfiguration
}

class GamepadControl(gamepad: Gamepad) {
    val a = GamepadButton(gamepad::a)
    val b = GamepadButton(gamepad::b)
    val x = GamepadButton(gamepad::x)
    val y = GamepadButton(gamepad::y)
    val leftBumper = GamepadButton(gamepad::left_bumper)
    val rightBumper = GamepadButton(gamepad::right_bumper)
    val leftTrigger = GamepadValue(gamepad::left_trigger)
    val rightTrigger = GamepadValue(gamepad::right_trigger)
}

class GamepadButton(private val property: KProperty0<Boolean>) {
    val isPressed get() = property.get()
}

class GamepadValue(private val property: KProperty0<Float>) {
    val value get() = property.get().toDouble()
}

class GamepadJoystick(
    private val xProperty: KProperty0<Float>,
    private val yProperty: KProperty0<Float>
) {

}

class DriverControlScope(gamepad0: Gamepad, gamepad1: Gamepad) {

    val driver: GamepadControl = GamepadControl(gamepad0)
    val gunner: GamepadControl = GamepadControl(gamepad1)

    fun loop(block: suspend () -> Unit) {

    }

}

suspend fun Robot.driverControl(scope: suspend DriverControlScope.() -> Unit) {
    perform {
        val (g0, g1) = feature(DriverControl)
        val driverControlScope = DriverControlScope(g0, g1)
    }
}