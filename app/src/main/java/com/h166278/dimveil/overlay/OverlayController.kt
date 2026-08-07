package com.h166278.dimveil.overlay

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.WindowManager

class OverlayController(private val context: Context, private val windowManager: WindowManager) {
    private var view: View? = null
    fun show(depth: Int, host: OverlayHostKind) {
        val params = OverlayWindowParams.create(depth, host)
        val layer = view ?: View(context).also { it.setBackgroundColor(Color.BLACK); view = it }
        layer.alpha = params.alpha
        val layout = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, params.type, params.flags, android.graphics.PixelFormat.TRANSLUCENT).apply { alpha = 1f }
        try {
            if (layer.parent == null) windowManager.addView(layer, layout) else windowManager.updateViewLayout(layer, layout)
        } catch (_: IllegalStateException) { remove(); windowManager.addView(layer, layout) }
    }
    fun update(depth: Int) { view?.alpha = depth.coerceIn(0, 90) / 100f }
    fun remove() { view?.let { if (it.parent != null) runCatching { windowManager.removeViewImmediate(it) } }; view = null }
}
