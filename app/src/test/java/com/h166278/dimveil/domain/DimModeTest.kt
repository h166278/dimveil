package com.h166278.dimveil.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DimModeTest {
    @Test fun presetsMatchApprovedDepths() {
        assertEquals(62, DimMode.NIGHT.defaultDepth)
        assertEquals(42, DimMode.READING.defaultDepth)
        assertEquals(28, DimMode.GAME.defaultDepth)
        assertEquals(50, DimMode.CUSTOM.defaultDepth)
    }
    @Test fun depthIsClampedToSafeRange() { assertEquals(0, DimMode.clamp(-1)); assertEquals(90, DimMode.clamp(91)) }
    @Test fun customModeUsesSavedDepth() { assertEquals(73, DimMode.CUSTOM.depth(73)); assertEquals(90, DimMode.CUSTOM.depth(100)) }
}
