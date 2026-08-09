package com.h166278.dimveil

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.h166278.dimveil.MainViewModel.ToggleOutcome
import com.h166278.dimveil.domain.AutoStartMode
import com.h166278.dimveil.domain.DimSettings
import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.overlay.AccessibilityOverlayHost
import com.h166278.dimveil.overlay.OverlayHostKind
import com.h166278.dimveil.overlay.OverlayPermissionAutoReturn
import com.h166278.dimveil.overlay.OverlayRuntime
import com.h166278.dimveil.overlay.ShizukuAccessibility
import com.h166278.dimveil.service.OverlayService
import com.h166278.dimveil.ui.DimVeilTheme
import com.h166278.dimveil.ui.HomeScreen
import com.h166278.dimveil.ui.TileGuideDialog
import rikka.shizuku.Shizuku
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private var pendingStartAfterGrant = false
    /** 自动开启无障碍遮罩时，Shizuku 授权弹窗同意后续跑自动开启流程 */
    private var pendingAutoAccessibilityStart = false
    /** 选择「自动无障碍」时，Shizuku 授权弹窗同意后自动开启无障碍权限 */
    private var pendingAutoAccessibilityGrant = false
    /** Shizuku 授权期间暂存的遮罩；授权流程结束后按条件恢复 */
    private var overlayRestore: OverlayRestore? = null
    /** 首次开启遮罩时展示磁贴引导弹窗 */
    private var showTileGuide by mutableStateOf(false)

    // Shizuku 授权结果：同意后自动完成这次想做的操作，无需再操作一次
    private val shizukuPermissionListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == ShizukuAccessibility.REQUEST_CODE) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    when {
                        // 自动开启无障碍遮罩流程：开权限 → 等服务连接 → 启动遮罩
                        pendingAutoAccessibilityStart -> {
                            pendingAutoAccessibilityStart = false
                            // 授权成功后由无障碍流程接管，不恢复旧的普通遮罩
                            overlayRestore = null
                            lifecycleScope.launch {
                                val saved = viewModel.waitForSettings()
                                autoStartAccessibility(saved)
                            }
                        }
                        // 选择「自动无障碍」：开权限，遮罩等下次进入自动启动
                        pendingAutoAccessibilityGrant -> {
                            pendingAutoAccessibilityGrant = false
                            lifecycleScope.launch {
                                grantAccessibilityAndNotify()
                                restoreOverlayAfterShizuku()
                            }
                        }
                        else -> performAccessibilityToggle()
                    }
                } else {
                    pendingAutoAccessibilityStart = false
                    pendingAutoAccessibilityGrant = false
                    restoreOverlayAfterShizuku()
                    Toast.makeText(this, R.string.shizuku_permission_denied, Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 授权页转屏/重建后仍记得"授权成功要自动启动遮罩"
        pendingStartAfterGrant = savedInstanceState?.getBoolean(KEY_PENDING_START, false) ?: false
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            DimVeilTheme {
                if (showTileGuide) {
                    TileGuideDialog(
                        onDismiss = { showTileGuide = false },
                        onAddTile = {
                            showTileGuide = false
                            openQuickSettingsPanel()
                        }
                    )
                }
                HomeScreen(
                    state = state,
                    onToggle = {
                        val wasActive = state.active
                        if (!viewModel.toggleOverlay()) {
                            if (state.accessibilityEnabled && !state.accessibilityReady) {
                                // 无障碍已开启但服务尚未连接（如系统重启后）：提示而非跳设置页
                                Toast.makeText(this, R.string.accessibility_connecting, Toast.LENGTH_SHORT).show()
                            } else if (state.accessibilityEnabled) {
                                // 记录自动返回：服务连接后把暗幕带回前台
                                AccessibilityOverlayHost.armAutoReturn(this)
                                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            } else {
                                openOverlayPermission()
                            }
                        } else if (!wasActive) {
                            // 遮罩从关闭到开启（首次手动开启）：介绍快捷设置磁贴
                            maybeShowTileGuide()
                        }
                    },
                    onMode = viewModel::selectMode,
                    onDepthPreview = viewModel::previewDepth,
                    onDepthCommit = viewModel::commitDepth,
                    onDoubleTapAccessibility = { performAccessibilityToggle() },
                    onAutoStartChange = { mode ->
                        viewModel.setAutoStartMode(mode)
                        if (mode == AutoStartMode.ACCESSIBILITY) ensureShizukuForAutoAccessibility()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
        super.onDestroy()
    }

    /** 双击空白处：经 Shizuku 直接切换无障碍授权，不跳设置页 */
    private fun performAccessibilityToggle() {
        lifecycleScope.launch {
            when (val outcome = viewModel.toggleAccessibility()) {
                is ToggleOutcome.Toggled -> {
                    restoreOverlayAfterShizuku()
                    Toast.makeText(
                        this@MainActivity,
                        if (outcome.nowEnabled) R.string.accessibility_turned_on else R.string.accessibility_turned_off,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                ToggleOutcome.NeedPermission -> {
                    // 授权弹窗不能与普通悬浮窗重叠：先停遮罩，授权结束后恢复
                    stopOverlayForShizuku()
                    ShizukuAccessibility.requestPermission()
                    Toast.makeText(this@MainActivity, R.string.shizuku_request_permission, Toast.LENGTH_SHORT).show()
                }
                ToggleOutcome.ShizukuUnavailable ->
                    Toast.makeText(this@MainActivity, R.string.shizuku_unavailable, Toast.LENGTH_SHORT).show()
                ToggleOutcome.Failed ->
                    Toast.makeText(this@MainActivity, R.string.accessibility_toggle_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_PENDING_START, pendingStartAfterGrant)
    }

    override fun onResume() {
        super.onResume()
        OverlayPermissionAutoReturn.disarm()
        viewModel.refreshPermissions()
        // 从悬浮窗授权页回来：授权成功则自动启动遮罩，免去再点一次开关
        if (pendingStartAfterGrant) {
            pendingStartAfterGrant = false
            if (OverlayService.canDraw(this) && !OverlayRuntime.state.value.active) {
                val s = viewModel.uiState.value
                OverlayService.start(this, s.depth, s.mode)
            }
        } else {
            // 「自动开启遮罩」：每次回到前台评估一次。
            // - 遮罩运行中（active=true）→ 不重复启动；
            // - 主页主开关手动关闭过 → 本次进程不再自动开启；
            // - 划掉任务重开 / 通知栏按钮关闭后回前台 → 自动开启。
            // 直接等 DataStore 真实设置（waitForSettings）：uiState 的 settings 是
            // stateIn(Eagerly)，会先发射默认值，冷启动时会命中假值导致漏触发；
            // DataStore 原始 flow 首次发射即磁盘真实值。
            lifecycleScope.launch {
                val saved = viewModel.waitForSettings()
                if (OverlayService.autoStartSuppressed || OverlayRuntime.state.value.active) return@launch
                when (saved.autoStartMode) {
                    AutoStartMode.OFF -> Unit
                    AutoStartMode.NORMAL -> {
                        if (OverlayService.canDraw(this@MainActivity)) {
                            OverlayService.start(
                                this@MainActivity, saved.depth, saved.mode, OverlayHostKind.NORMAL
                            )
                        }
                    }
                    AutoStartMode.ACCESSIBILITY -> autoStartAccessibility(saved)
                }
            }
        }
    }

    private data class OverlayRestore(
        val depth: Int,
        val mode: DimMode,
        val host: OverlayHostKind?
    )

    /** Shizuku 系统授权期间停止所有遮罩，避免授权弹窗被覆盖。 */
    private fun stopOverlayForShizuku() {
        if (overlayRestore != null || !OverlayRuntime.state.value.active) return
        val state = OverlayRuntime.state.value
        overlayRestore = OverlayRestore(state.requestedDepth, state.mode, state.host)
        OverlayService.stop(this)
    }

    /** 仅在授权流程结束后恢复授权前正在运行的遮罩，并尊重当前设置。 */
    private fun restoreOverlayAfterShizuku() {
        val restore = overlayRestore ?: return
        overlayRestore = null
        if (viewModel.uiState.value.autoStartMode == AutoStartMode.OFF) return
        if (OverlayRuntime.state.value.active) return
        val host = when (restore.host) {
            OverlayHostKind.NORMAL -> if (OverlayService.canDraw(this)) restore.host else null
            OverlayHostKind.ACCESSIBILITY -> if (AccessibilityOverlayHost.available) restore.host else null
            null -> null
        }
        OverlayService.start(this, restore.depth, restore.mode, host)
    }

    /**
     * 用户选择「自动无障碍」时立即准备：确保 Shizuku 已授权并开启无障碍权限，
     * 这样下次重新打开暗幕时即可直接自动启动无障碍遮罩。
     * - 权限已开：无需操作；
     * - Shizuku 已授权：直接开启权限；
     * - Shizuku 未授权：弹授权申请，同意后自动开启权限；
     * - Shizuku 未运行：提示，下次进入时走系统设置页半自动流程。
     */
    private fun ensureShizukuForAutoAccessibility() {
        if (viewModel.uiState.value.accessibilityEnabled) return
        when {
            ShizukuAccessibility.isAvailable() && ShizukuAccessibility.isGranted() -> {
                lifecycleScope.launch { grantAccessibilityAndNotify() }
            }
            ShizukuAccessibility.isAvailable() -> {
                pendingAutoAccessibilityGrant = true
                stopOverlayForShizuku()
                ShizukuAccessibility.requestPermission()
                Toast.makeText(this, R.string.shizuku_request_permission, Toast.LENGTH_SHORT).show()
                // 兜底：弹窗无响应时降级到系统设置页
                armGrantTimeout()
            }
            else -> {
                Toast.makeText(this, R.string.shizuku_unavailable, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 开启无障碍权限并提示结果（授权已就绪时调用） */
    private suspend fun grantAccessibilityAndNotify() {
        if (viewModel.enableAccessibility()) {
            Toast.makeText(this, R.string.auto_accessibility_enabled, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.accessibility_toggle_failed, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shizuku 授权弹窗兜底：30 秒内未收到授权回调（弹窗无响应/服务异常）时，
     * 降级到系统设置页手动开启无障碍权限，避免流程卡死。
     */
    private fun armGrantTimeout() {
        lifecycleScope.launch {
            delay(30_000)
            val stillPending = pendingAutoAccessibilityStart || pendingAutoAccessibilityGrant
            if (!stillPending) return@launch
            pendingAutoAccessibilityStart = false
            pendingAutoAccessibilityGrant = false
            // 即将打开系统无障碍设置，保持遮罩停止，避免再次覆盖系统页面
            Toast.makeText(this@MainActivity, R.string.auto_accessibility_grant_timeout, Toast.LENGTH_SHORT).show()
            AccessibilityOverlayHost.armAutoReturn(this@MainActivity)
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    /**
     * 自动开启无障碍遮罩：先确保无障碍权限与服务连接，再以无障碍遮罩启动。
     * - 服务已连接：直接启动；
     * - 权限已开但服务未连接（如系统重启后）：等待绑定；
     * - 权限未开：经 Shizuku 自动开启（未授权则弹授权申请，同意后回调续跑）；
     * - 无 Shizuku：跳系统设置页由用户手动开启，返回后 onResume 会再次评估。
     */
    private suspend fun autoStartAccessibility(saved: DimSettings) {
        val st = viewModel.uiState.value
        when {
            st.accessibilityReady -> {
                OverlayService.start(this, saved.depth, saved.mode, OverlayHostKind.ACCESSIBILITY)
            }
            st.accessibilityEnabled -> {
                if (waitForAccessibilityReady()) {
                    OverlayService.start(this, saved.depth, saved.mode, OverlayHostKind.ACCESSIBILITY)
                } else {
                    Toast.makeText(this, R.string.auto_accessibility_connect_timeout, Toast.LENGTH_SHORT).show()
                }
            }
            ShizukuAccessibility.isAvailable() && ShizukuAccessibility.isGranted() -> {
                if (!viewModel.enableAccessibility()) {
                    Toast.makeText(this, R.string.accessibility_toggle_failed, Toast.LENGTH_SHORT).show()
                    return
                }
                if (waitForAccessibilityReady()) {
                    OverlayService.start(this, saved.depth, saved.mode, OverlayHostKind.ACCESSIBILITY)
                } else {
                    Toast.makeText(this, R.string.auto_accessibility_connect_timeout, Toast.LENGTH_SHORT).show()
                }
            }
            ShizukuAccessibility.isAvailable() -> {
                // 已运行但未授权：弹授权申请，同意后回调继续自动开启
                pendingAutoAccessibilityStart = true
                stopOverlayForShizuku()
                ShizukuAccessibility.requestPermission()
                Toast.makeText(this, R.string.shizuku_request_permission, Toast.LENGTH_SHORT).show()
                // 兜底：Shizuku 授权弹窗偶发无响应（fork 版/服务状态异常），
                // 30 秒未收到回调则降级到系统设置页手动开启
                armGrantTimeout()
            }
            else -> {
                // 无 Shizuku：跳系统设置页手动开启，返回后 onResume 再次评估
                Toast.makeText(this, R.string.auto_accessibility_need_manual, Toast.LENGTH_SHORT).show()
                AccessibilityOverlayHost.armAutoReturn(this)
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
    }

    /** 等待无障碍服务连接（最多 15 秒），连接后返回 true */
    private suspend fun waitForAccessibilityReady(): Boolean = withTimeoutOrNull(15_000) {
        AccessibilityOverlayHost.availability.first { it }
        true
    } ?: false

    private fun openOverlayPermission() {
        OverlayPermissionAutoReturn.arm(this)
        pendingStartAfterGrant = true
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
        )
    }

    /**
     * 首次开启遮罩时弹窗介绍快捷设置磁贴（作用 + 手动添加引导）。
     * 平台不允许应用自动添加磁贴，只能引导用户手动拖入快捷面板。
     */
    private fun maybeShowTileGuide() {
        lifecycleScope.launch {
            if (viewModel.consumeTileGuideShown()) {
                showTileGuide = true
            }
        }
    }

    /** 打开快捷设置面板（用户点击编辑按钮拖入磁贴）；低版本退回文案引导 */
    private fun openQuickSettingsPanel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching {
                // Settings.Panel.ACTION_QUICK_SETTINGS 在部分构建环境解析异常，
                // 直接使用其公开常量值（API 30+），行为等价
                startActivity(Intent("android.settings.panel.action.QUICK_SETTINGS"))
            }.onFailure {
                Toast.makeText(this, R.string.tile_guide_manual_hint, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, R.string.tile_guide_manual_hint, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val KEY_PENDING_START = "pending_start_after_grant"
    }
}
