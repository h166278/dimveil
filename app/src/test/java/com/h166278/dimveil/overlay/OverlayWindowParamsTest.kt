package com.h166278.dimveil.overlay

import android.view.WindowManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayWindowParamsTest {
    @Test fun blackAlphaMapsToSafeDepth() { assertEquals(0f, OverlayWindowParams.create(0, OverlayHostKind.NORMAL).alpha); assertEquals(.9f, OverlayWindowParams.create(90, OverlayHostKind.NORMAL).alpha); assertEquals(.9f, OverlayWindowParams.create(100, OverlayHostKind.NORMAL).alpha) }
    @Test fun normalOverlayIsTouchThrough() { val p=OverlayWindowParams.create(62, OverlayHostKind.NORMAL); assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,p.type); assertTrue(p.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE != 0); assertTrue(p.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE != 0) }
    @Test fun accessibilityTypeIsSelected() { assertEquals(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,OverlayWindowParams.create(42, OverlayHostKind.ACCESSIBILITY).type) }
}
