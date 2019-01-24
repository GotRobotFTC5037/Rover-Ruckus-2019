package org.firstinspires.ftc.teamcode.lib.util

import com.qualcomm.ftccommon.ConfigWifiDirectActivity.launch
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.lib.action.ActionScope

inline fun ActionScope.loop(crossinline block: suspend () -> Unit) {
    launch {
        while (true) {
            block.invoke()
            yield()
        }
    }
}