package com.h166278.dimveil.overlay

import com.h166278.dimveil.domain.DimMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayRuntimeTest {
    @Test
    fun runningStateContainsActualHostAndDepth() {
        OverlayRuntime.running(90, 80, DimMode.NIGHT, OverlayHostKind.NORMAL)
        val state = OverlayRuntime.state.value
        assertTrue(state.active)
        assertEquals(90, state.requestedDepth)
        assertEquals(80, state.appliedDepth)
        assertEquals(OverlayHostKind.NORMAL, state.host)
        OverlayRuntime.stopped()
    }

    @Test
    fun failureClearsActiveState() {
        OverlayRuntime.failed(OverlayError.WINDOW_REJECTED)
        val state = OverlayRuntime.state.value
        assertFalse(state.active)
        assertEquals(OverlayError.WINDOW_REJECTED, state.error)
        OverlayRuntime.stopped()
    }
}
