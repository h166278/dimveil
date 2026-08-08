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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var active by remember { mutableStateOf(false) }
            var mode by remember { mutableStateOf(DimMode.NIGHT) }
            var depth by remember { mutableStateOf(DimMode.NIGHT.defaultDepth) }
            var accessibilityEnabled by remember { mutableStateOf(isAccessibilityEnabled()) }
            val canDraw = OverlayService.canDraw(this)

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
        // Compose refreshes the icon when the activity returns from system settings.
    }

    private fun isAccessibilityEnabled(): Boolean {
        val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val expected = ComponentName(this, com.h166278.dimveil.service.DimAccessibilityService::class.java).flattenToString()
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.resolveInfo.serviceInfo.let { info -> ComponentName(info.packageName, info.name).flattenToString() == expected } }
    }
}
