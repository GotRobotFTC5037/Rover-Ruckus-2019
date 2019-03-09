package org.firstinspires.ftc.teamcode.lib.feature.localizer

import org.firstinspires.ftc.robotcore.external.navigation.Orientation

interface HeadingLocalizer {
    suspend fun heading(): Double
}

interface OrientationLocalizer : HeadingLocalizer {
    suspend fun orientation(): Orientation

}
