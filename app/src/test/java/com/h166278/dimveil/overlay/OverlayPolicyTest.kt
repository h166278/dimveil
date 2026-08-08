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
}
