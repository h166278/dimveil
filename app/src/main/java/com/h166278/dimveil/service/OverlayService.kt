package com.h166278.dimveil.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.h166278.dimveil.data.DataStoreDimPreferences
import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.overlay.AccessibilityOverlayHost
import com.h166278.dimveil.overlay.OverlayController
import com.h166278.dimveil.overlay.OverlayError
import com.h166278.dimveil.overlay.OverlayHostKind
import com.h166278.dimveil.overlay.OverlayPolicy
import com.h166278.dimveil.overlay.OverlayRuntime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OverlayService : Service() {
    private lateinit var normalController: OverlayController
    private var requestedDepth = 0
    private var mode = DimMode.NIGHT
    private var foregroundStarted = false
    /** 自动启动指定的遮罩类型；null = 按 OverlayPolicy 自动选择（手动开关等默认路径） */
    private var preferredHost: OverlayHostKind? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        normalController = OverlayController(this, getSystemService(WindowManager::class.java))
        DimNotificationFactory.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                requestedDepth = intent.getIntExtra(EXTRA_DEPTH, 0).coerceIn(0, 90)
                mode = intent.getStringExtra(EXTRA_MODE)
                    ?.let { name -> DimMode.entries.firstOrNull { it.name == name } }
                    ?: DimMode.NIGHT
                // 自动启动可指定遮罩类型；手动开关启动不带该 extra（null = 自动选择）
                preferredHost = intent.getStringExtra(EXTRA_HOST)
                    ?.let { name -> OverlayHostKind.entries.firstOrNull { it.name == name } }
                if (!ensureForeground()) return START_STICKY
                applyBestHost()
            }
            ACTION_UPDATE -> {
                requestedDepth = intent.getIntExtra(EXTRA_DEPTH, 0).coerceIn(0, 90)
                mode = intent.getStringExtra(EXTRA_MODE)
                    ?.let { name -> DimMode.entries.firstOrNull { it.name == name } }
                    ?: DimMode.NIGHT
                if (!ensureForeground()) return START_STICKY
                applyBestHost()
            }
            ACTION_HOST_CHANGED -> if (OverlayRuntime.state.value.active) applyBestHost()
            // 关闭遮罩：仅主页主开关关闭（manual=true）抑制本次进程的自动开启；
            // 通知栏按钮关闭、划掉任务等被动停止不抑制，回到前台仍会自动开启
            ACTION_STOP -> {
                if (intent.getBooleanExtra(EXTRA_MANUAL_STOP, false)) autoStartSuppressed = true
                stopOverlay()
            }
            // 进程被系统回收后重建：从持久化设置恢复遮罩
            null -> restoreAndResume()
        }
        // START_STICKY：遮罩是用户主动开启的保护性覆盖，进程被系统回收后应自动恢复，
        // 而不是静默消失（重建时 onStartCommand(null) 走恢复路径）。
        return START_STICKY
    }

    private fun restoreAndResume() {
        if (OverlayRuntime.state.value.active) return
        serviceScope.launch {
            val settings = DataStoreDimPreferences(this@OverlayService).settings.first()
            requestedDepth = settings.depth
            mode = settings.mode
            withContext(Dispatchers.Main) {
                if (!ensureForeground()) return@withContext
                applyBestHost()
            }
        }
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
        // 指定类型仅在对应能力可用时生效，否则回退自动选择
        val targetHost = preferredHost?.let { host ->
            when (host) {
                OverlayHostKind.NORMAL -> if (canDraw(this)) host else null
                OverlayHostKind.ACCESSIBILITY -> if (AccessibilityOverlayHost.available) host else null
            }
        } ?: OverlayPolicy.selectHost(
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
                DimTileService.requestUpdate(this)
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
        DimTileService.requestUpdate(this)
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopOverlay()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        normalController.remove()
        serviceScope.cancel()
        if (OverlayRuntime.state.value.active) {
            OverlayRuntime.stopped()
            DimTileService.requestUpdate(this)
        }
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
        private const val EXTRA_MANUAL_STOP = "manual_stop"
        private const val EXTRA_HOST = "host"

        /**
         * 本次进程内用户通过主页主开关手动关闭过遮罩。
         * 「进入自动开启遮罩」在本次运行期间不再生效，仅重新启动 app（新进程）后恢复。
         * 通知栏按钮关闭 / 划掉任务等被动停止不会置位。
         */
        @Volatile
        var autoStartSuppressed = false

        fun canDraw(context: Context): Boolean = Settings.canDrawOverlays(context)

        fun startIntent(
            context: Context,
            depth: Int,
            mode: DimMode,
            host: OverlayHostKind? = null
        ) = commandIntent(context, ACTION_START, depth, mode).apply {
            if (host != null) putExtra(EXTRA_HOST, host.name)
        }

        fun updateIntent(context: Context, depth: Int, mode: DimMode) =
            commandIntent(context, ACTION_UPDATE, depth, mode)

        fun stopIntent(context: Context, manual: Boolean = false) =
            Intent(context, OverlayService::class.java)
                .setAction(ACTION_STOP)
                .putExtra(EXTRA_MANUAL_STOP, manual)

        /** 启动遮罩服务；[host] 非空时强制使用指定遮罩类型（自动开启用），null = 自动选择 */
        fun start(context: Context, depth: Int, mode: DimMode, host: OverlayHostKind? = null) {
            ContextCompat.startForegroundService(context, startIntent(context, depth, mode, host))
        }

        fun update(context: Context, depth: Int, mode: DimMode) {
            context.startService(updateIntent(context, depth, mode))
        }

        fun stop(context: Context, manual: Boolean = false) {
            context.startService(stopIntent(context, manual))
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
