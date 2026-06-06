package com.example.fluidoverlay

internal enum class BatteryIcon {
    FULL, CHARGING, BAR_6, BAR_5, BAR_4, BAR_3, BAR_2, BAR_1, BAR_0
}

internal fun batteryIcon(pct: Int, charging: Boolean): BatteryIcon = when {
    pct == 100 -> BatteryIcon.FULL
    charging   -> BatteryIcon.CHARGING
    pct > 85   -> BatteryIcon.BAR_6
    pct > 71   -> BatteryIcon.BAR_5
    pct > 57   -> BatteryIcon.BAR_4
    pct > 42   -> BatteryIcon.BAR_3
    pct > 28   -> BatteryIcon.BAR_2
    pct > 14   -> BatteryIcon.BAR_1
    else       -> BatteryIcon.BAR_0
}

internal fun batteryPct(level: Int, scale: Int): Int? {
    if (level < 0 || scale <= 0) return null
    return (level * 100 / scale.toFloat()).toInt()
}
