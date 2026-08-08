package com.h166278.dimveil.overlay

import android.content.Context
import android.graphics.Color
import android.hardware.input.InputManager
import android.os.Build
import android.view.View
import android.view.WindowManager
import kotlin.math.floor

class OverlayController(
    private val context: Context,
    private val windowManager: WindowManager
) {
    private var view: View? = null

    fun show(depth: Int, host: OverlayHostKind): Result<Int> = runCatching {
        val maxDepth = if (host == OverlayHostKind.NORMAL) {
            normalMaxDepth(context)
        } else {
            OverlayWindowParams.MAX_DEPTH
        }
        val params = OverlayWindowParams.create(depth, host, maxDepth)
        val layer = view ?: View(context).also {
            it.setBackgroundColor(Color.BLACK)
            view = it
        }
        layer.alpha = 1f
        val layout = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            params.type,
            params.flags,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            alpha = params.alpha
        }
        try {
            if (layer.parent == null) {
                windowManager.addView(layer, layout)
            } else {
                windowManager.updateViewLayout(layer, layout)
            }
        } catch (_: IllegalStateException) {
            remove()
            windowManager.addView(layer, layout)
        }
        (params.alpha * 100).toInt()
    }

    fun remove() {
        view?.let { layer ->
            if (layer.parent != null) runCatching { windowManager.removeViewImmediate(layer) }
        }
        view = null
    }

    companion object {
        fun normalMaxDepth(context: Context): Int {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                return OverlayWindowParams.MAX_DEPTH
            }
            val opacity = context.getSystemService(InputManager::class.java)
                .maximumObscuringOpacityForTouch
            return floor(opacity * 100f).toInt()
                .coerceIn(0, OverlayWindowParams.NORMAL_MAX_DEPTH)
        }
    }
}
