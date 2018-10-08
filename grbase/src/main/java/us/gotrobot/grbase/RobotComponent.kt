package us.gotrobot.grbase

import com.qualcomm.robotcore.hardware.HardwareMap

typealias Component = RobotComponent
typealias ComponentConfiguration = RobotComponentConfiguration
typealias ComponentInstallerKey<C> = RobotComponentInstallerKey<C>
typealias ComponentInstaller<TConfiguration, TComponent> =
        RobotComponentInstaller<TConfiguration, TComponent>

@DslMarker
annotation class RobotComponentMarker

@RobotComponentMarker
interface RobotComponent

@RobotComponentMarker
interface RobotComponentConfiguration {
    val hardwareMap: HardwareMap
}

class RobotComponentInstallerKey<C : Component>(val name: String)

interface RobotComponentInstaller<TConfiguration : RobotComponentConfiguration, TComponent : Component> {
    val key: RobotComponentInstallerKey<TComponent>
    fun install(robot: Robot, configure: TConfiguration.() -> Unit): TComponent
}
