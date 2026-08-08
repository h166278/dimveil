package com.h166278.dimveil.overlay

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.h166278.dimveil.MainActivity

/**
 * 悬浮窗授权自动返回：跳转系统授权页（ACTION_MANAGE_OVERLAY_PERMISSION）前 arm，
 * 轮询 canDrawOverlays，一旦授权成功就把暗幕带回前台，省去手动返回设置页。
 *
 * 使用 Handler 而非 lifecycleScope：授权页覆盖本应用时 Activity 处于 STOPPED，
 * lifecycle 感知协程会被暂停，无法轮询。
 */
object OverlayPermissionAutoReturn {
    private const val POLL_INTERVAL_MS = 400L
    private const val DEFAULT_TIMEOUT_MS = 120_000L

    private val handler = Handler(Looper.getMainLooper())
    private var deadline = 0L
    private var polling = false

    /** 跳转系统悬浮窗授权页前调用 */
    fun arm(context: Context, timeoutMillis: Long = DEFAULT_TIMEOUT_MS) {
        deadline = System.currentTimeMillis() + timeoutMillis
        if (polling) return
        polling = true
        val appContext = context.applicationContext
        handler.post(object : Runnable {
            override fun run() {
                if (!polling) return
                if (System.currentTimeMillis() > deadline) {
                    polling = false
                    return
                }
                if (Settings.canDrawOverlays(appContext)) {
                    polling = false
                    appContext.startActivity(
                        Intent(appContext, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        }
                    )
                    return
                }
                handler.postDelayed(this, POLL_INTERVAL_MS)
            }
        })
    }

    /** 回到暗幕时调用（无论手动返回还是自动拉回），停止轮询与等待窗口 */
    fun disarm() {
        polling = false
        deadline = 0
    }
}
