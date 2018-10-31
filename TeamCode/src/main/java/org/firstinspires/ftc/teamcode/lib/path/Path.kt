package org.firstinspires.ftc.teamcode.lib.path

interface Path {

    interface Element: Path {
        operator fun plus(element: Element): Path = TODO()
    }
}

