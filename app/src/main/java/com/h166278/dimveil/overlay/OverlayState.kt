package com.h166278.dimveil.overlay

import com.h166278.dimveil.domain.DimMode

enum class OverlayHostKind { NORMAL, ACCESSIBILITY }

enum class OverlayError { NO_AVAILABLE_HOST, WINDOW_REJECTED, FOREGROUND_START_FAILED }

data class OverlayState(
    val active: Boolean = false,
    val requestedDepth: Int = 0,
    val appliedDepth: Int = 0,
    val mode: DimMode = DimMode.NIGHT,
    val host: OverlayHostKind? = null,
    val error: OverlayError? = null
)
