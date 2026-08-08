package com.h166278.dimveil

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.h166278.dimveil.overlay.AccessibilityOverlayHost
import com.h166278.dimveil.overlay.OverlayPermissionAutoReturn
import com.h166278.dimveil.overlay.OverlayRuntime
import com.h166278.dimveil.service.OverlayService
import com.h166278.dimveil.ui.DimVeilTheme
import com.h166278.dimveil.ui.HomeScreen

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private var pendingStartAfterGrant = false
    private var notificationDeniedPermanently = false

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // 拒绝且系统不再建议解释 = 用户已选择"不再询问"，之后不再发起无效请求
        if (!granted && !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            notificationDeniedPermanently = true
        }
        viewModel.refreshPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 授权页转屏/重建后仍记得"授权成功要自动启动遮罩"
        pendingStartAfterGrant = savedInstanceState?.getBoolean(KEY_PENDING_START, false) ?: false
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            DimVeilTheme {
                HomeScreen(
                    state = state,
                    onToggle = {
                        val wasActive = state.active
                        if (viewModel.toggleOverlay()) {
                            if (!wasActive) {
                                requestNotificationPermissionIfNeeded(state.notificationsAllowed)
                            }
                        } else if (state.accessibilityEnabled && !state.accessibilityReady) {
                            // 无障碍已开启但服务尚未连接（如系统重启后）：提示而非跳设置页
                            Toast.makeText(this, R.string.accessibility_connecting, Toast.LENGTH_SHORT).show()
                        } else if (state.accessibilityEnabled) {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        } else {
                            openOverlayPermission()
                        }
                    },
                    onMode = viewModel::selectMode,
                    onDepthPreview = viewModel::previewDepth,
                    onDepthCommit = viewModel::commitDepth,
                    onOpenAccessibility = {
                        if (!state.accessibilityEnabled) AccessibilityOverlayHost.armAutoReturn(this)
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    onDoubleTapAccessibility = {
                        if (viewModel.toggleAccessibility()) {
                            // 已开启 → 请求系统禁用本服务，遮罩回退悬浮窗路径
                            Toast.makeText(this, R.string.accessibility_disabled, Toast.LENGTH_SHORT).show()
                        } else {
                            // 未开启 → 系统不允许应用自动开启，跳设置页由用户手动拨开
                            AccessibilityOverlayHost.armAutoReturn(this)
                            Toast.makeText(this, R.string.accessibility_enable_hint, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    }
                )
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
        }
    }

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

    private fun requestNotificationPermissionIfNeeded(alreadyAllowed: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !alreadyAllowed && !notificationDeniedPermanently
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        private const val KEY_PENDING_START = "pending_start_after_grant"
    }
}
