package com.h166278.dimveil.overlay

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import java.lang.ref.WeakReference

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

    /** 跳转系统悬浮窗授权页前调用。保留原任务，授权成功后直接恢复它。 */
    fun arm(activity: Activity, timeoutMillis: Long = DEFAULT_TIMEOUT_MS) {
        deadline = System.currentTimeMillis() + timeoutMillis
        if (polling) return
        polling = true
        val activityRef = WeakReference(activity)
        handler.post(object : Runnable {
            override fun run() {
                if (!polling) return
                if (System.currentTimeMillis() > deadline) {
                    polling = false
                    return
                }
                val owner = activityRef.get()
                if (owner == null || owner.isFinishing || owner.isDestroyed) {
                    polling = false
                    return
                }
                if (Settings.canDrawOverlays(owner)) {
                    polling = false
                    // 不从后台启动新 Activity；直接恢复刚才跳走的暗幕任务。
                    owner.appTask.moveToFront()
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
