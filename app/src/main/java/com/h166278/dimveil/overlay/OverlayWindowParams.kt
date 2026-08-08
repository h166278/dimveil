package com.h166278.dimveil.overlay

import android.graphics.Color
import android.view.WindowManager

data class OverlayWindowParams(val alpha: Float, val flags: Int, val type: Int) {
    companion object {
        fun create(
            depth: Int,
            host: OverlayHostKind,
            normalMaxDepth: Int = NORMAL_MAX_DEPTH
        ): OverlayWindowParams {
            val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            val type = if (host == OverlayHostKind.ACCESSIBILITY) {
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            }
            val maxDepth = if (host == OverlayHostKind.ACCESSIBILITY) {
                MAX_DEPTH
            } else {
                normalMaxDepth.coerceIn(0, MAX_DEPTH)
            }
            return OverlayWindowParams(depth.coerceIn(0, maxDepth) / 100f, flags, type)
        }

        const val BLACK = Color.BLACK
        const val MAX_DEPTH = 90
        const val NORMAL_MAX_DEPTH = 80
    }
}
