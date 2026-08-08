package com.h166278.dimveil

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.h166278.dimveil.data.DataStoreDimPreferences
import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.service.OverlayService
import com.h166278.dimveil.ui.DimVeilTheme
import com.h166278.dimveil.ui.HomeScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val preferences by lazy { DataStoreDimPreferences(this) }

    // 类级 Compose 状态：可在 onResume 等生命周期回调中刷新，UI 自动重组
    private var active by mutableStateOf(false)
    private var mode by mutableStateOf(DimMode.NIGHT)
    private var depth by mutableStateOf(DimMode.NIGHT.defaultDepth)
    private var accessibilityEnabled by mutableStateOf(false)
    private var canDraw by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshPermissionStates()
        setContent {
            LaunchedEffect(Unit) {
                val settings = preferences.settings.first()
                mode = settings.mode
                depth = settings.depth
            }
            DimVeilTheme {
                HomeScreen(
                    context = this,
                    active = active,
                    mode = mode,
                    depth = depth,
                    accessibilityEnabled = accessibilityEnabled,
                    canDraw = canDraw,
                    onToggle = {
                        if (active) {
                            OverlayService.stop(this)
                            active = false
                        } else if (canDraw) {
                            OverlayService.start(this, depth, mode)
                            active = true
                        } else {
                            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                        }
                    },
                    onMode = { selected ->
                        mode = selected
                        depth = selected.depth(depth)
                        lifecycleScope.launch { preferences.selectMode(selected) }
                    },
                    onDepth = { value ->
                        depth = value.coerceIn(0, 90)
                        lifecycleScope.launch { preferences.setDepth(depth) }
                        if (active) OverlayService.start(this, depth, mode)
                    },
                    onAccessibilityRefresh = { accessibilityEnabled = isAccessibilityEnabled() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 从系统权限/无障碍设置返回后刷新状态，UI 立即反映最新权限
        refreshPermissionStates()
    }

    private fun refreshPermissionStates() {
        canDraw = OverlayService.canDraw(this)
        accessibilityEnabled = isAccessibilityEnabled()
    }

    private fun isAccessibilityEnabled(): Boolean {
        val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val expected = ComponentName(this, com.h166278.dimveil.service.DimAccessibilityService::class.java).flattenToString()
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.resolveInfo.serviceInfo.let { info -> ComponentName(info.packageName, info.name).flattenToString() == expected } }
    }
}
