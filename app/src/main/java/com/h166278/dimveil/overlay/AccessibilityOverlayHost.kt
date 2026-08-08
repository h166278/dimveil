package com.h166278.dimveil.overlay

import android.content.Context
import com.h166278.dimveil.service.OverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AccessibilityOverlayHost {
    private var controller: OverlayController? = null
    private val mutableAvailable = MutableStateFlow(false)
    private var pendingReturnDeadline = 0L

    /** 跳转系统无障碍设置前调用：开启成功后自动回到暗幕 */
    fun armAutoReturn(timeoutMillis: Long = 60_000) {
        pendingReturnDeadline = System.currentTimeMillis() + timeoutMillis
    }

    /** 无障碍服务连接后由服务调用：命中等待窗口则带回暗幕，并清除标记 */
    fun consumeAutoReturn(): Boolean {
        val armed = System.currentTimeMillis() < pendingReturnDeadline
        pendingReturnDeadline = 0
        return armed
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
