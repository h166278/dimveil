package com.h166278.dimveil.ui

import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.overlay.OverlayError
import com.h166278.dimveil.overlay.OverlayHostKind

data class MainUiState(
    val active: Boolean = false,
    val mode: DimMode = DimMode.NIGHT,
    val depth: Int = DimMode.NIGHT.defaultDepth,
    val appliedDepth: Int = 0,
    val host: OverlayHostKind? = null,
    val accessibilityEnabled: Boolean = false,
    val accessibilityReady: Boolean = false,
    val canDraw: Boolean = false,
    val notificationsAllowed: Boolean = true,
    val normalMaxDepth: Int = 80,
    val autoStart: Boolean = false,
    val error: OverlayError? = null
) {
    val canStart: Boolean get() = accessibilityReady || canDraw
    val depthLimited: Boolean
        get() = active && host == OverlayHostKind.NORMAL && appliedDepth < depth
}
