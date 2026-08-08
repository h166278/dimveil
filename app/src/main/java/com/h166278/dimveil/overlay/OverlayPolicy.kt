package com.h166278.dimveil.overlay

object OverlayPolicy {
    fun selectHost(accessibilityReady: Boolean, canDraw: Boolean): OverlayHostKind? = when {
        accessibilityReady -> OverlayHostKind.ACCESSIBILITY
        canDraw -> OverlayHostKind.NORMAL
        else -> null
    }

    fun appliedDepth(requestedDepth: Int, host: OverlayHostKind, normalMaxDepth: Int): Int {
        val maxDepth = if (host == OverlayHostKind.ACCESSIBILITY) {
            OverlayWindowParams.MAX_DEPTH
        } else {
            normalMaxDepth.coerceIn(0, OverlayWindowParams.MAX_DEPTH)
        }
        return requestedDepth.coerceIn(0, maxDepth)
    }
}
