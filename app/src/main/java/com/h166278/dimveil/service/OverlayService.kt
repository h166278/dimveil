package com.h166278.dimveil.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.overlay.AccessibilityOverlayHost
import com.h166278.dimveil.overlay.OverlayController
import com.h166278.dimveil.overlay.OverlayError
import com.h166278.dimveil.overlay.OverlayHostKind
import com.h166278.dimveil.overlay.OverlayPolicy
import com.h166278.dimveil.overlay.OverlayRuntime

class OverlayService : Service() {
    private lateinit var normalController: OverlayController
    private var requestedDepth = 0
    private var mode = DimMode.NIGHT
    private var foregroundStarted = false

    override fun onCreate() {
        super.onCreate()
        normalController = OverlayController(this, getSystemService(WindowManager::class.java))
        DimNotificationFactory.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START, ACTION_UPDATE -> {
                requestedDepth = intent.getIntExtra(EXTRA_DEPTH, 0).coerceIn(0, 90)
                mode = intent.getStringExtra(EXTRA_MODE)
                    ?.let { name -> DimMode.entries.firstOrNull { it.name == name } }
                    ?: DimMode.NIGHT
                if (!ensureForeground()) return START_NOT_STICKY
                applyBestHost()
            }
            ACTION_HOST_CHANGED -> if (OverlayRuntime.state.value.active) applyBestHost()
            ACTION_STOP -> stopOverlay()
            null -> stopOverlay()
        }
        return START_NOT_STICKY
    }

    private fun ensureForeground(): Boolean = runCatching {
        startForeground(
            DimNotificationFactory.NOTIFICATION_ID,
            DimNotificationFactory.notification(this, requestedDepth, mode.label)
        )
        foregroundStarted = true
    }.fold(
        onSuccess = { true },
        onFailure = {
            OverlayRuntime.failed(OverlayError.FOREGROUND_START_FAILED)
            stopSelf()
            false
        }
    )

    private fun applyBestHost() {
        val targetHost = OverlayPolicy.selectHost(
            accessibilityReady = AccessibilityOverlayHost.available,
            canDraw = canDraw(this)
        ) ?: run {
            OverlayRuntime.failed(OverlayError.NO_AVAILABLE_HOST)
            stopOverlay(preserveError = true)
            return
        }
        val result = if (targetHost == OverlayHostKind.ACCESSIBILITY) {
            AccessibilityOverlayHost.show(requestedDepth)
        } else {
            normalController.show(requestedDepth, OverlayHostKind.NORMAL)
        }
        result.fold(
            onSuccess = { appliedDepth ->
                if (targetHost == OverlayHostKind.ACCESSIBILITY) {
                    normalController.remove()
                } else {
                    AccessibilityOverlayHost.hide()
                }
                OverlayRuntime.running(requestedDepth, appliedDepth, mode, targetHost)
                refreshNotification(appliedDepth)
            },
            onFailure = {
                OverlayRuntime.failed(OverlayError.WINDOW_REJECTED)
                stopOverlay(preserveError = true)
            }
        )
    }

    private fun refreshNotification(appliedDepth: Int) {
        if (!foregroundStarted) return
        getSystemService(android.app.NotificationManager::class.java).notify(
            DimNotificationFactory.NOTIFICATION_ID,
            DimNotificationFactory.notification(this, appliedDepth, mode.label)
        )
    }

    private fun stopOverlay(preserveError: Boolean = false) {
        normalController.remove()
        AccessibilityOverlayHost.hide()
        if (!preserveError) OverlayRuntime.stopped()
        if (foregroundStarted) stopForeground(STOP_FOREGROUND_REMOVE)
        foregroundStarted = false
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopOverlay()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        normalController.remove()
        if (OverlayRuntime.state.value.active) OverlayRuntime.stopped()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val ACTION_START = "com.h166278.dimveil.START"
        private const val ACTION_UPDATE = "com.h166278.dimveil.UPDATE"
        private const val ACTION_STOP = "com.h166278.dimveil.STOP"
        private const val ACTION_HOST_CHANGED = "com.h166278.dimveil.HOST_CHANGED"
        private const val EXTRA_DEPTH = "depth"
        private const val EXTRA_MODE = "mode"

        fun canDraw(context: Context): Boolean = Settings.canDrawOverlays(context)

        fun startIntent(context: Context, depth: Int, mode: DimMode) =
            commandIntent(context, ACTION_START, depth, mode)

        fun updateIntent(context: Context, depth: Int, mode: DimMode) =
            commandIntent(context, ACTION_UPDATE, depth, mode)

        fun stopIntent(context: Context) =
            Intent(context, OverlayService::class.java).setAction(ACTION_STOP)

        fun start(context: Context, depth: Int, mode: DimMode) {
            ContextCompat.startForegroundService(context, startIntent(context, depth, mode))
        }

        fun update(context: Context, depth: Int, mode: DimMode) {
            context.startService(updateIntent(context, depth, mode))
        }

        fun stop(context: Context) {
            context.startService(stopIntent(context))
        }

        fun hostChanged(context: Context) {
            if (!OverlayRuntime.state.value.active) return
            context.startService(
                Intent(context, OverlayService::class.java).setAction(ACTION_HOST_CHANGED)
            )
        }

        private fun commandIntent(
            context: Context,
            action: String,
            depth: Int,
            mode: DimMode
        ) = Intent(context, OverlayService::class.java)
            .setAction(action)
            .putExtra(EXTRA_DEPTH, depth)
            .putExtra(EXTRA_MODE, mode.name)
    }
}
