package com.h166278.dimveil.overlay

import android.app.Activity
import android.app.ActivityManager
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
 *
 * 注意：轮询不依赖 Activity 实例存活（快照 applicationContext + taskId）——
 * MIUI 等系统在权限页覆盖期间可能回收本应用 Activity，若轮询持有旧实例
 * 引用会在 isDestroyed 时停摆，导致授权成功也无法自动返回。
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
        // 快照上下文与任务 id：Activity 在权限页期间被回收也不影响恢复
        val appContext = activity.applicationContext
        val taskId = activity.taskId
        handler.post(object : Runnable {
            override fun run() {
                if (!polling) return
                if (System.currentTimeMillis() > deadline) {
                    polling = false
                    return
                }
                if (Settings.canDrawOverlays(appContext)) {
                    polling = false
                    restoreTask(appContext, taskId)
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

    /**
     * 恢复暗幕主页到前台。
     *
     * MIUI 会把 Settings 的授权 Activity 压入暗幕自己的任务栈，单纯 moveToFront()
     * 只会把仍以 Settings 为顶部 Activity 的任务带到前台。必须在原任务中以
     * CLEAR_TOP 启动 MainActivity，先清掉 Settings 页面，再恢复主页。
     */
    private fun restoreTask(context: Context, taskId: Int) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appTask = am.appTasks.firstOrNull { it.taskInfo.id == taskId }
        val restored = appTask != null && runCatching {
            appTask.startActivity(context, homeIntent(context), null)
            appTask.moveToFront()
            true
        }.getOrDefault(false)
        if (restored) return
        // 原任务已被清理：新建任务带回主页（MIUI 可能拦截后台启动，尽力而为）
        runCatching { context.startActivity(homeIntent(context, Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    private fun homeIntent(context: Context, extraFlags: Int = 0) =
        Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or extraFlags)
        }
}
