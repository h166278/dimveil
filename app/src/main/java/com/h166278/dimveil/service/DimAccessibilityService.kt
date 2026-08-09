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
        val moved = appTask != null && runCatching {
            // 复用原任务，避免后台 startActivity 被 MIUI 等系统拦截
            appTask.moveToFront()
            true
        }.getOrDefault(false)
        if (moved) return
        // 原任务已被清理（如系统回收）或 moveToFront 受限：新建任务带回
        runCatching {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
            )
        }
    }

    private fun detachHost() {
        controller?.let { AccessibilityOverlayHost.detach(this, it) }
        controller = null
    }
}
