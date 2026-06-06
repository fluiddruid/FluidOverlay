package com.example.fluidoverlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BatteryIconTest {

    // ── batteryIcon ──────────────────────────────────────────────────────────

    @Test fun pct100_notCharging_returnsFull() =
        assertEquals(BatteryIcon.FULL, batteryIcon(100, false))

    @Test fun pct100_charging_returnsFull() =
        assertEquals(BatteryIcon.FULL, batteryIcon(100, true))

    @Test fun charging_below100_returnsCharging() =
        assertEquals(BatteryIcon.CHARGING, batteryIcon(99, true))

    @Test fun charging_midLevel_returnsCharging() =
        assertEquals(BatteryIcon.CHARGING, batteryIcon(50, true))

    @Test fun charging_at0_returnsCharging() =
        assertEquals(BatteryIcon.CHARGING, batteryIcon(0, true))

    // bar 6: pct > 85
    @Test fun pct99_returnsBar6() = assertEquals(BatteryIcon.BAR_6, batteryIcon(99, false))
    @Test fun pct86_returnsBar6() = assertEquals(BatteryIcon.BAR_6, batteryIcon(86, false))
    @Test fun pct85_returnsBar5() = assertEquals(BatteryIcon.BAR_5, batteryIcon(85, false))

    // bar 5: pct > 71
    @Test fun pct72_returnsBar5() = assertEquals(BatteryIcon.BAR_5, batteryIcon(72, false))
    @Test fun pct71_returnsBar4() = assertEquals(BatteryIcon.BAR_4, batteryIcon(71, false))

    // bar 4: pct > 57
    @Test fun pct58_returnsBar4() = assertEquals(BatteryIcon.BAR_4, batteryIcon(58, false))
    @Test fun pct57_returnsBar3() = assertEquals(BatteryIcon.BAR_3, batteryIcon(57, false))

    // bar 3: pct > 42
    @Test fun pct43_returnsBar3() = assertEquals(BatteryIcon.BAR_3, batteryIcon(43, false))
    @Test fun pct42_returnsBar2() = assertEquals(BatteryIcon.BAR_2, batteryIcon(42, false))

    // bar 2: pct > 28
    @Test fun pct29_returnsBar2() = assertEquals(BatteryIcon.BAR_2, batteryIcon(29, false))
    @Test fun pct28_returnsBar1() = assertEquals(BatteryIcon.BAR_1, batteryIcon(28, false))

    // bar 1: pct > 14
    @Test fun pct15_returnsBar1() = assertEquals(BatteryIcon.BAR_1, batteryIcon(15, false))
    @Test fun pct14_returnsBar0() = assertEquals(BatteryIcon.BAR_0, batteryIcon(14, false))

    // bar 0: else
    @Test fun pct1_returnsBar0()  = assertEquals(BatteryIcon.BAR_0, batteryIcon(1, false))
    @Test fun pct0_returnsBar0()  = assertEquals(BatteryIcon.BAR_0, batteryIcon(0, false))

    // ── batteryPct ───────────────────────────────────────────────────────────

    @Test fun normalPct()           = assertEquals(75,  batteryPct(75, 100))
    @Test fun zeroPct()             = assertEquals(0,   batteryPct(0, 100))
    @Test fun fullPct()             = assertEquals(100, batteryPct(100, 100))
    @Test fun fractionalPct()       = assertEquals(75,  batteryPct(3, 4))
    @Test fun negativeLevel_null()  = assertNull(batteryPct(-1, 100))
    @Test fun zeroScale_null()      = assertNull(batteryPct(50, 0))
    @Test fun negativeScale_null()  = assertNull(batteryPct(50, -1))
}
