package com.h166278.dimveil.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OverlayPolicyTest {
    @Test
    fun accessibilityHostHasPriority() {
        assertEquals(
            OverlayHostKind.ACCESSIBILITY,
            OverlayPolicy.selectHost(accessibilityReady = true, canDraw = true)
        )
    }

    @Test
    fun normalHostIsFallback() {
        assertEquals(
            OverlayHostKind.NORMAL,
            OverlayPolicy.selectHost(accessibilityReady = false, canDraw = true)
        )
        assertNull(OverlayPolicy.selectHost(accessibilityReady = false, canDraw = false))
    }

    @Test
    fun depthFollowsHostSafetyLimit() {
        assertEquals(80, OverlayPolicy.appliedDepth(90, OverlayHostKind.NORMAL, 80))
        assertEquals(90, OverlayPolicy.appliedDepth(90, OverlayHostKind.ACCESSIBILITY, 80))
        assertEquals(0, OverlayPolicy.appliedDepth(-1, OverlayHostKind.NORMAL, 80))
    }
}
