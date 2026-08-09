package com.h166278.dimveil.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.h166278.dimveil.MainActivity
import com.h166278.dimveil.data.DataStoreDimPreferences
import com.h166278.dimveil.overlay.AccessibilityOverlayHost
import com.h166278.dimveil.overlay.OverlayRuntime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 快捷设置磁贴：下拉快捷设置面板即可一键开启/关闭遮罩。
 * 系统级常驻入口，不依赖通知权限（通知被拒绝时通知栏按钮不可用，磁贴仍可用）。
 *
 * 注意：Android 不允许应用编程式添加磁贴到快捷面板，
 * 首次使用需用户手动拖入（首次开启遮罩时有引导弹窗）。
 */
class DimTileService : TileService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onStartListening() {
        super.onStartListening()
        syncState()
    }

    override fun onClick() {
        super.onClick()
        if (isLocked()) {
            // 锁屏状态下磁贴需解锁后再执行，避免锁屏误触开关
            unlockAndRun { toggle() }
        } else {
            toggle()
        }
    }

    private fun toggle() {
        val state = OverlayRuntime.state.value
        if (state.active) {
            // 与主页主开关一致的语义：手动关闭，本次进程内不再自动开启
            OverlayService.stop(this, manual = true)
        } else {
            scope.launch {
                val settings = DataStoreDimPreferences(this@DimTileService).settings.first()
                if (OverlayService.canDraw(this@DimTileService) || AccessibilityOverlayHost.available) {
                    OverlayService.start(this@DimTileService, settings.depth, settings.mode)
                } else {
                    // 无任何可用权限：打开主页引导授权
                    startActivity(
                        Intent(this@DimTileService, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        }
                    )
                }
            }
        }
    }

    /** 根据当前遮罩运行状态刷新磁贴图标/文字 */
    private fun syncState() {
        qsTile?.state = if (OverlayRuntime.state.value.active) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }
        qsTile?.updateTile()
    }

    companion object {
        /** 遮罩状态变化时通知系统刷新磁贴（下拉面板时展示最新状态） */
        fun requestUpdate(context: Context) {
            TileService.requestListeningState(
                context,
                ComponentName(context, DimTileService::class.java)
            )
        }
    }
}
