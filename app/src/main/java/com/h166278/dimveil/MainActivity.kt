package com.h166278.dimveil

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.h166278.dimveil.overlay.AccessibilityOverlayHost
import com.h166278.dimveil.ui.DimVeilTheme
import com.h166278.dimveil.ui.HomeScreen

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        if (!state.accessibilityEnabled) AccessibilityOverlayHost.armAutoReturn()
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }

    private fun openOverlayPermission() {
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
        )
    }

    private fun requestNotificationPermissionIfNeeded(alreadyAllowed: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !alreadyAllowed) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
