package com.h166278.dimveil.overlay

import android.view.WindowManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayWindowParamsTest {
    @Test
    fun normalOverlayIsCappedAtTouchThroughLimit() {
        assertEquals(0f, OverlayWindowParams.create(0, OverlayHostKind.NORMAL).alpha)
        assertEquals(.8f, OverlayWindowParams.create(90, OverlayHostKind.NORMAL).alpha)
        assertEquals(.76f, OverlayWindowParams.create(90, OverlayHostKind.NORMAL, 76).alpha)
    }

    @Test
    fun accessibilityOverlayAllowsFullApprovedDepth() {
        assertEquals(.9f, OverlayWindowParams.create(90, OverlayHostKind.ACCESSIBILITY).alpha)
        assertEquals(.9f, OverlayWindowParams.create(100, OverlayHostKind.ACCESSIBILITY).alpha)
    }

    @Test
    fun normalOverlayIsTouchThrough() {
        val params = OverlayWindowParams.create(62, OverlayHostKind.NORMAL)
        assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, params.type)
        assertTrue(params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE != 0)
        assertTrue(params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE != 0)
        assertTrue(params.flags and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS != 0)
    }

    @Test
    fun accessibilityTypeIsSelected() {
        assertEquals(
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            OverlayWindowParams.create(42, OverlayHostKind.ACCESSIBILITY).type
        )
    }
}
