package com.h166278.dimveil.service

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.h166278.dimveil.MainActivity
import com.h166278.dimveil.overlay.AccessibilityOverlayHost
import com.h166278.dimveil.overlay.OverlayController

class DimAccessibilityService : AccessibilityService() {
    private var controller: OverlayController? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        val overlayController = OverlayController(
            this,
            getSystemService(WindowManager::class.java)
        )
        controller = overlayController
        AccessibilityOverlayHost.attach(this, overlayController)
        val taskId = AccessibilityOverlayHost.consumeAutoReturn()
        if (taskId >= 0) restoreTask(taskId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        detachHost()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        detachHost()
        super.onDestroy()
    }

    private fun restoreTask(taskId: Int) {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appTask = am.appTasks.firstOrNull { it.taskInfo.id == taskId }
        val restored = appTask != null && runCatching {
            // Settings 页面会压入暗幕任务栈；CLEAR_TOP 先清掉它们，再恢复主页。
            appTask.startActivity(this, homeIntent(), null)
            appTask.moveToFront()
            true
        }.getOrDefault(false)
        if (restored) return
        // 原任务已被清理或 AppTask 恢复失败：新建任务带回主页
        runCatching { startActivity(homeIntent(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    private fun homeIntent(extraFlags: Int = 0) =
        Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or extraFlags)
        }

    private fun detachHost() {
        controller?.let { AccessibilityOverlayHost.detach(this, it) }
        controller = null
    }
}
