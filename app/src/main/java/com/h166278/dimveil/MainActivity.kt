package com.h166278.dimveil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.lifecycle.lifecycleScope
import com.h166278.dimveil.MainViewModel.ToggleOutcome
import com.h166278.dimveil.overlay.AccessibilityOverlayHost
import com.h166278.dimveil.overlay.OverlayPermissionAutoReturn
import com.h166278.dimveil.overlay.OverlayRuntime
import com.h166278.dimveil.overlay.ShizukuAccessibility
import com.h166278.dimveil.service.OverlayService
import com.h166278.dimveil.ui.DimVeilTheme
import com.h166278.dimveil.ui.HomeScreen
import rikka.shizuku.Shizuku
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

    // Shizuku 授权结果：同意后自动完成这次双击想做的切换，无需再双击一次
    private val shizukuPermissionListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == ShizukuAccessibility.REQUEST_CODE) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    performAccessibilityToggle()
                } else {
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
                    onDoubleTapAccessibility = { performAccessibilityToggle() },
                    onAutoStartChange = viewModel::setAutoStart
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
                is ToggleOutcome.Toggled -> Toast.makeText(
                    this@MainActivity,
                    if (outcome.nowEnabled) R.string.accessibility_turned_on else R.string.accessibility_turned_off,
                    Toast.LENGTH_SHORT
                ).show()
                ToggleOutcome.NeedPermission -> {
                    // 首次使用：弹 Shizuku 授权申请，同意后自动完成切换
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
        }
        // 「进入自动开启遮罩」：仅在本次进程（冷启动）评估一次，回到前台不重复触发；
        // 本次运行期间用户手动关过遮罩则不再自动开启，重新启动 app 后恢复。
        // uiState 首个值由 DataStore 异步读取，必须等流发射后再判断，
        // 否则冷启动时读到默认值会漏触发；canDraw 直接同步查系统权限保证实时。
        lifecycleScope.launch {
            val s = viewModel.uiState.first()
            if (s.autoStart && !autoStartEvaluated && !OverlayService.autoStartSuppressed &&
                !OverlayRuntime.state.value.active &&
                (OverlayService.canDraw(this@MainActivity) || s.accessibilityReady)
            ) {
                OverlayService.start(this@MainActivity, s.depth, s.mode)
            }
            autoStartEvaluated = true
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

        /** 本次进程内是否已评估过「进入自动开启遮罩」：冷启动后只评估一次，回到前台不重复 */
        @Volatile
        private var autoStartEvaluated = false
    }
}
