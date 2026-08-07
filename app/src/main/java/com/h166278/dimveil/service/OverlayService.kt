package com.h166278.dimveil.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.provider.Settings
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.overlay.OverlayController
import com.h166278.dimveil.overlay.OverlayHostKind

class OverlayService : Service() {
    private lateinit var controller: OverlayController
    override fun onCreate() { super.onCreate(); controller = OverlayController(this, getSystemService(WindowManager::class.java)); DimNotificationFactory.createChannel(this) }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START, ACTION_UPDATE -> {
                val depth = intent.getIntExtra(EXTRA_DEPTH, 0).coerceIn(0, 90)
                val mode = intent.getStringExtra(EXTRA_MODE) ?: DimMode.NIGHT.label
                controller.show(depth, OverlayHostKind.NORMAL)
                startForeground(DimNotificationFactory.NOTIFICATION_ID, DimNotificationFactory.notification(this, depth, mode))
            }
            ACTION_STOP -> stopOverlay()
        }
        return START_NOT_STICKY
    }
    private fun stopOverlay() { controller.remove(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
    override fun onTaskRemoved(rootIntent: Intent?) { stopOverlay(); super.onTaskRemoved(rootIntent) }
    override fun onDestroy() { controller.remove(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
    companion object {
        private const val ACTION_START = "com.h166278.dimveil.START"
        private const val ACTION_UPDATE = "com.h166278.dimveil.UPDATE"
        private const val ACTION_STOP = "com.h166278.dimveil.STOP"
        private const val EXTRA_DEPTH = "depth"
        private const val EXTRA_MODE = "mode"
        fun canDraw(context: Context) = Settings.canDrawOverlays(context)
        fun startIntent(context: Context, depth: Int, mode: DimMode) = Intent(context, OverlayService::class.java).setAction(ACTION_START).putExtra(EXTRA_DEPTH, depth).putExtra(EXTRA_MODE, mode.label)
        fun stopIntent(context: Context) = Intent(context, OverlayService::class.java).setAction(ACTION_STOP)
        fun start(context: Context, depth: Int, mode: DimMode) = ContextCompat.startForegroundService(context, startIntent(context, depth, mode))
        fun stop(context: Context) = context.startService(stopIntent(context))
        fun notificationAllowed(context: Context) = android.os.Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
}
