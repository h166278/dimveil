package com.h166278.dimveil

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import kotlinx.coroutines.launch

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

    fun selectMode(selected: DimMode) {
        val selectedDepth = selected.depth(settings.value.customDepth)
        previewDepth.value = null
        modeOverride.value = selected
        if (OverlayRuntime.state.value.active) {
            OverlayService.update(app, selectedDepth, selected)
        }
        viewModelScope.launch {
            preferences.selectMode(selected)
            modeOverride.value = null
        }
    }

    fun previewDepth(value: Int) {
        val safe = DimMode.clamp(value)
        previewDepth.value = safe
        val mode = uiState.value.mode
        if (OverlayRuntime.state.value.active) {
            OverlayService.update(app, safe, mode)
        }
    }

    fun commitDepth() {
        val value = previewDepth.value ?: return
        viewModelScope.launch {
            preferences.setDepth(value)
            previewDepth.value = null
        }
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
