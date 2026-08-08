package com.h166278.dimveil

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.h166278.dimveil.data.DataStoreDimPreferences
import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.domain.DimSettings
import com.h166278.dimveil.overlay.AccessibilityOverlayHost
import com.h166278.dimveil.overlay.OverlayController
import com.h166278.dimveil.overlay.OverlayRuntime
import com.h166278.dimveil.service.DimAccessibilityService
import com.h166278.dimveil.service.OverlayService
import com.h166278.dimveil.ui.MainUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private data class Permissions(
        val canDraw: Boolean = false,
        val accessibilityEnabled: Boolean = false,
        val notificationsAllowed: Boolean = true
    )

    private val app = application.applicationContext
    private val preferences = DataStoreDimPreferences(app)
    private val permissions = MutableStateFlow(Permissions())
    private val previewDepth = MutableStateFlow<Int?>(null)
    private val modeOverride = MutableStateFlow<DimMode?>(null)

    // 深度滑杆节流：服务端窗口更新昂贵（IPC + updateViewLayout），
    // 拖动时按移动量/时间合并发送，UI 预览保持即时。
    private var lastDepthSent = -1
    private var lastDepthSentAt = 0L

    private val settings = preferences.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        DimSettings()
    )

    private val presentedSettings = combine(
        settings,
        modeOverride,
        previewDepth
    ) { saved, pendingMode, pendingDepth ->
        val mode = pendingMode ?: saved.mode
        DimSettings(
            mode = mode,
            depth = pendingDepth ?: if (pendingMode != null) {
                mode.depth(saved.customDepth)
            } else {
                saved.depth
            },
            customDepth = saved.customDepth
        )
    }

    val uiState = combine(
        presentedSettings,
        OverlayRuntime.state,
        permissions,
        AccessibilityOverlayHost.availability
    ) { presented, overlay, permission, accessibilityReady ->
        MainUiState(
            active = overlay.active,
            mode = presented.mode,
            depth = presented.depth,
            appliedDepth = overlay.appliedDepth,
            host = overlay.host,
            accessibilityEnabled = permission.accessibilityEnabled,
            accessibilityReady = accessibilityReady,
            canDraw = permission.canDraw,
            notificationsAllowed = permission.notificationsAllowed,
            normalMaxDepth = OverlayController.normalMaxDepth(app),
            error = overlay.error
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState())

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        permissions.value = Permissions(
            canDraw = OverlayService.canDraw(app),
            accessibilityEnabled = isAccessibilityEnabled(),
            notificationsAllowed = notificationsAllowed()
        )
    }

    fun toggleOverlay(): Boolean {
        val state = uiState.value
        if (state.active) {
            OverlayService.stop(app)
            return true
        }
        if (!state.canStart) return false
        OverlayService.start(app, state.depth, state.mode)
        return true
    }

    /**
     * 双击空白处切换无障碍权限：
     * 已开启 → 请求系统禁用本服务（[DimAccessibilityService.disable]），返回 true；
     * 未开启 → 返回 false，由调用方跳转系统无障碍设置页引导用户手动开启
     * （Android 不允许应用编程式开启无障碍服务）。
     */
    fun toggleAccessibility(): Boolean {
        if (!isAccessibilityEnabled()) return false
        DimAccessibilityService.disable()
        // disableSelf 异步生效：稍候再刷新权限状态，避免界面仍显示"已开启"
        viewModelScope.launch {
            delay(600)
            refreshPermissions()
        }
        return true
    }

    fun selectMode(selected: DimMode) {
        val selectedDepth = selected.depth(settings.value.customDepth)
        previewDepth.value = null
        modeOverride.value = selected
        resetDepthThrottle()
        if (OverlayRuntime.state.value.active) {
            OverlayService.update(app, selectedDepth, selected)
        }
        viewModelScope.launch {
            preferences.selectMode(selected)
            // 仅当用户未继续切换模式时清除 override，避免快速连点时 UI 回跳
            if (modeOverride.value == selected) modeOverride.value = null
        }
    }

    fun previewDepth(value: Int) {
        val safe = DimMode.clamp(value)
        previewDepth.value = safe
        pushDepthUpdate(safe)
    }

    fun commitDepth() {
        val value = previewDepth.value ?: return
        viewModelScope.launch {
            preferences.setDepth(value)
            previewDepth.value = null
            // 节流可能吞掉拖动中间值，松手时强制补发最终深度
            if (OverlayRuntime.state.value.active) {
                OverlayService.update(app, value, uiState.value.mode)
            }
        }
    }

    /** 拖动节流：位移 ≥3% 或距上次发送 ≥150ms 才推送，避免滑杆每帧触发一次 IPC */
    private fun pushDepthUpdate(depth: Int) {
        if (!OverlayRuntime.state.value.active) return
        val now = SystemClock.elapsedRealtime()
        val moved = abs(depth - lastDepthSent)
        if (moved >= 3 || now - lastDepthSentAt >= 150L) {
            lastDepthSent = depth
            lastDepthSentAt = now
            OverlayService.update(app, depth, uiState.value.mode)
        }
    }

    private fun resetDepthThrottle() {
        lastDepthSent = -1
        lastDepthSentAt = 0L
    }

    private fun isAccessibilityEnabled(): Boolean {
        val manager = app.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val expected = ComponentName(app, DimAccessibilityService::class.java)
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { info ->
                val serviceInfo = info.resolveInfo.serviceInfo
                ComponentName(serviceInfo.packageName, serviceInfo.name) == expected
            }
    }

    private fun notificationsAllowed(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                app,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
}
