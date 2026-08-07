package com.h166278.dimveil.overlay

enum class OverlayHostKind { NORMAL, ACCESSIBILITY }

data class OverlayState(val active: Boolean = false, val depth: Int = 0, val host: OverlayHostKind? = null)
