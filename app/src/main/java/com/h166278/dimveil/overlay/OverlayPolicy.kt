package com.h166278.dimveil.overlay

object OverlayPolicy {
    fun selectHost(accessibilityReady: Boolean, canDraw: Boolean): OverlayHostKind? = when {
        accessibilityReady -> OverlayHostKind.ACCESSIBILITY
        canDraw -> OverlayHostKind.NORMAL
        else -> null
    }
}
