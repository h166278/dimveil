package com.h166278.dimveil.overlay

import android.app.Activity
import android.content.Context
import com.h166278.dimveil.service.OverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AccessibilityOverlayHost {
    private var controller: OverlayController? = null
    private val mutableAvailable = MutableStateFlow(false)
    private var pendingReturnDeadline = 0L
    private var pendingReturnTaskId = -1

    /** 跳转系统无障碍设置前调用：开启成功后自动回到暗幕 */
    fun armAutoReturn(activity: Activity? = null, timeoutMillis: Long = 60_000) {
        pendingReturnDeadline = System.currentTimeMillis() + timeoutMillis
        // 记录目标任务 id：服务连接后经 ActivityManager 恢复任务，
        // 避免在 MIUI 等系统上从后台直接 startActivity 被后台启动策略拦截
        pendingReturnTaskId = activity?.taskId ?: -1
    }

    /** 无障碍服务连接后由服务调用：命中等待窗口则返回目标任务 id（-1 = 未命中/已过期） */
    fun consumeAutoReturn(): Int {
        val armed = System.currentTimeMillis() < pendingReturnDeadline
        val taskId = if (armed) pendingReturnTaskId else -1
        pendingReturnDeadline = 0
        pendingReturnTaskId = -1
        return taskId
    }

    val available: Boolean get() = controller != null
    val availability: StateFlow<Boolean> = mutableAvailable.asStateFlow()

    fun attach(context: Context, value: OverlayController) {
        controller?.remove()
        controller = value
        mutableAvailable.value = true
        OverlayService.hostChanged(context)
    }

    fun detach(context: Context, value: OverlayController) {
        if (controller === value) {
            value.remove()
            controller = null
            mutableAvailable.value = false
            OverlayService.hostChanged(context)
        }
    }

    fun show(depth: Int): Result<Int> = controller
        ?.show(depth, OverlayHostKind.ACCESSIBILITY)
        ?: Result.failure(IllegalStateException("Accessibility overlay host is unavailable"))

    fun hide() {
        controller?.remove()
    }
}
