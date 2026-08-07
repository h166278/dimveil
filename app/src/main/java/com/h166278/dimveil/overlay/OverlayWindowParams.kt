package com.h166278.dimveil.overlay

import android.graphics.Color
import android.view.WindowManager

data class OverlayWindowParams(val alpha: Float, val flags: Int, val type: Int) {
    companion object {
        fun create(depth: Int, host: OverlayHostKind): OverlayWindowParams {
            val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            val type = if (host == OverlayHostKind.ACCESSIBILITY) WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            return OverlayWindowParams(depth.coerceIn(0, 90) / 100f, flags, type)
        }
        const val BLACK = Color.BLACK
    }
}
