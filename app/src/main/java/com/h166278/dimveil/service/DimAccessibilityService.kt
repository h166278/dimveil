package com.h166278.dimveil.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.h166278.dimveil.overlay.OverlayController
import com.h166278.dimveil.overlay.OverlayHostKind

class DimAccessibilityService : AccessibilityService() {
    private var controller: OverlayController? = null
    private val commands = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_SHOW -> controller?.show(intent.getIntExtra(EXTRA_DEPTH, 0), OverlayHostKind.ACCESSIBILITY)
                ACTION_HIDE -> controller?.remove()
            }
        }
    }
    override fun onServiceConnected() {
        controller = OverlayController(this, getSystemService(WindowManager::class.java))
        ContextCompat.registerReceiver(this, commands, IntentFilter().apply { addAction(ACTION_SHOW); addAction(ACTION_HIDE) }, ContextCompat.RECEIVER_NOT_EXPORTED)
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
    override fun onDestroy() { runCatching { unregisterReceiver(commands) }; controller?.remove(); super.onDestroy() }
    companion object {
        private const val ACTION_SHOW = "com.h166278.dimveil.ACCESSIBILITY_SHOW"
        private const val ACTION_HIDE = "com.h166278.dimveil.ACCESSIBILITY_HIDE"
        private const val EXTRA_DEPTH = "depth"
        fun showIntent(context: Context, depth: Int) = Intent(ACTION_SHOW).setPackage(context.packageName).putExtra(EXTRA_DEPTH, depth)
        fun hideIntent(context: Context) = Intent(ACTION_HIDE).setPackage(context.packageName)
    }
}
