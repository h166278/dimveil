package com.h166278.dimveil.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.h166278.dimveil.R

/**
 * 首次开启遮罩时的磁贴引导弹窗：
 * 说明快捷设置磁贴的作用（下拉面板一键开关、不依赖通知权限），
 * 并引导用户手动添加到快捷设置面板（平台不允许应用自动添加）。
 */
@Composable
fun TileGuideDialog(onDismiss: () -> Unit, onAddTile: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Bolt, contentDescription = null) },
        title = { Text(stringResource(R.string.tile_guide_title)) },
        text = { Text(stringResource(R.string.tile_guide_message)) },
        confirmButton = {
            TextButton(onClick = onAddTile) { Text(stringResource(R.string.tile_guide_add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.tile_guide_later)) }
        }
    )
}
