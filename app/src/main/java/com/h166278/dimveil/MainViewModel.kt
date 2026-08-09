package com.h166278.dimveil

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.os.SystemClock
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.h166278.dimveil.data.DataStoreDimPreferences
import com.h166278.dimveil.domain.AutoStartMode
import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.domain.DimSettings
import com.h166278.dimveil.overlay.AccessibilityOverlayHost
import com.h166278.dimveil.overlay.OverlayController
import com.h166278.dimveil.overlay.OverlayRuntime
import com.h166278.dimveil.overlay.ShizukuAccessibility
import com.h166278.dimveil.service.DimAccessibilityService
import com.h166278.dimveil.service.OverlayService
import com.h166278.dimveil.ui.MainUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainViewModel(application: Application) : AndroidViewModel(application) {
    /** 双击切换无障碍授权的结果 */
    sealed interface ToggleOutcome {
        /** 切换成功，[nowEnabled] 为切换后的授权状态 */
        data class Toggled(val nowEnabled: Boolean) : ToggleOutcome
        /** Shizuku 已运行但未授权本应用，需要弹授权申请 */
        data object NeedPermission : ToggleOutcome
        /** Shizuku 未运行 */
        data object ShizukuUnavailable : ToggleOutcome
        /** 执行系统命令失败 */
        data object Failed : ToggleOutcome
    }

    private data class Permissions(
        val canDraw: Boolean = false,
        val accessibilityEnabled: Boolean = false
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
            customDepth = saved.customDepth,
            autoStartMode = saved.autoStartMode
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
            normalMaxDepth = OverlayController.normalMaxDepth(app),
            autoStartMode = presented.autoStartMode,
            error = overlay.error
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState())

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        permissions.value = Permissions(
            canDraw = OverlayService.canDraw(app),
            accessibilityEnabled = isAccessibilityEnabled()
        )
    }

    fun toggleOverlay(): Boolean {
        val state = uiState.value
        if (state.active) {
            // 主页主开关关闭：标记为手动关闭，本次进程内不再自动开启遮罩
            OverlayService.stop(app, manual = true)
            return true
        }
        if (!state.canStart) return false
        OverlayService.start(app, state.depth, state.mode)
        return true
    }

    /** 选择「自动开启遮罩」模式，持久化到 DataStore */
    fun setAutoStartMode(mode: AutoStartMode) {
        viewModelScope.launch {
            preferences.setAutoStartMode(mode)
        }
    }

    /**
     * 等待 DataStore 设置读取完成。
     * 直接订阅 DataStore 原始 flow（冷流，首次发射即磁盘真实值，无默认值），
     * 不用 stateIn 的 settings——后者 Eagerly 会先发默认 DimSettings()，无法与真实值区分。
     */
    suspend fun waitForSettings(): DimSettings = preferences.settings.first()

    /** 首次开启遮罩时消费磁贴引导（仅第一次返回 true） */
    suspend fun consumeTileGuideShown(): Boolean = preferences.consumeTileGuideShown()

    /** 经 Shizuku 自动开启无障碍授权；返回是否成功 */
    suspend fun enableAccessibility(): Boolean = ShizukuAccessibility.turnOn()

    /**
     * 双击空白处切换无障碍授权（等价系统无障碍快捷方式）：
     * 通过 Shizuku 直接修改系统无障碍服务列表，无需跳转设置页。
     * 返回切换结果：[ToggleOutcome.Toggled] 携带切换后的状态。
     */
    suspend fun toggleAccessibility(): ToggleOutcome {
        if (!ShizukuAccessibility.isAvailable()) return ToggleOutcome.ShizukuUnavailable
        if (!ShizukuAccessibility.isGranted()) return ToggleOutcome.NeedPermission
        return try {
            val nowEnabled = ShizukuAccessibility.toggle()
            // 系统异步绑定/解绑服务，稍候再刷新权限状态
            delay(700)
            refreshPermissions()
            ToggleOutcome.Toggled(nowEnabled)
        } catch (e: Exception) {
            ToggleOutcome.Failed
        }
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

}
