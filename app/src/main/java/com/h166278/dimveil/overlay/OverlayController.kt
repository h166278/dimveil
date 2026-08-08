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
        val layout = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            params.type,
            params.flags,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            // 遮罩深度由窗口级 alpha 承担（触摸穿透判定读取窗口 alpha）。
            alpha = params.alpha
            // Overlay windows must opt out of system-bar insets to dim the status bar.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) fitInsetsTypes = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
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
        /** 系统触摸穿透上限（普通覆盖最大安全深度）。该值在系统运行期间恒定，进程内缓存。 */
        @Volatile
        private var cachedNormalMaxDepth: Int? = null

        fun normalMaxDepth(context: Context): Int {
            cachedNormalMaxDepth?.let { return it }
            val value = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                OverlayWindowParams.MAX_DEPTH
            } else {
                val opacity = context.getSystemService(InputManager::class.java)
                    .maximumObscuringOpacityForTouch
                floor(opacity * 100f).toInt()
                    .coerceIn(0, OverlayWindowParams.NORMAL_MAX_DEPTH)
            }
            cachedNormalMaxDepth = value
            return value
        }
    }
}
