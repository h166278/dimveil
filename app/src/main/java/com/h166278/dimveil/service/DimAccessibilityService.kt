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
        instance = this
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
        instance = null
        detachHost()
        super.onDestroy()
    }

    private fun restoreTask(taskId: Int) {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appTask = am.appTasks.firstOrNull { it.taskInfo.id == taskId }
        if (appTask != null) {
            // 复用原任务，避免后台 startActivity 被 MIUI 等系统拦截
            appTask.moveToFront()
        } else {
            // 原任务已被清理（如系统回收）：新建任务带回
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

    companion object {
        @Volatile
        private var instance: DimAccessibilityService? = null

        /**
         * 请求系统禁用本无障碍服务（[AccessibilityService.disableSelf] 官方 API）。
         * 仅服务运行（绑定）期间有效；禁用成功后 [onUnbind]/[onDestroy] 会
         * detach 宿主，由 [AccessibilityOverlayHost] 通知 [com.h166278.dimveil.service.OverlayService]
         * 重路由回普通悬浮窗遮罩。
         */
        fun disable() {
            instance?.disableSelf()
        }
    }
}
