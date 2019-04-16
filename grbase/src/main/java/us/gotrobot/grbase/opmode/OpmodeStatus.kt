package us.gotrobot.grbase.opmode

interface OpmodeStatus {
    val isInitialized: Boolean
    val isStarted: Boolean
    val isStopped: Boolean
}