package org.firstinspires.ftc.teamcode.lib.util

import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareMap
import kotlin.reflect.KClass

operator fun <T : HardwareDevice> HardwareMap.get(clazz: KClass<T>, name: String): T =
    this.get(clazz.java, name)