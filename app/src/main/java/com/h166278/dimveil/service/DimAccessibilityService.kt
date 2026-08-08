package com.h166278.dimveil.service

import android.accessibilityservice.AccessibilityService
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.h166278.dimveil.overlay.AccessibilityOverlayHost
import com.h166278.dimveil.overlay.OverlayController

class DimAccessibilityService : AccessibilityService() {
    private var controller: OverlayController? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        val overlayController = OverlayController(
            this,
            getSystemService(WindowManager::class.java)
        )
        controller = overlayController
        AccessibilityOverlayHost.attach(this, overlayController)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        detachHost()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        detachHost()
        super.onDestroy()
    }

    private fun detachHost() {
        controller?.let { AccessibilityOverlayHost.detach(this, it) }
        controller = null
    }
}
