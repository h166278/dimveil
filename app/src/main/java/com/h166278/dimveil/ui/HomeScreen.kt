package com.h166278.dimveil.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.Window
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h166278.dimveil.domain.DimMode

private val Ink = Color(0xFF070B0D)
private val Panel = Color(0xFF101719)
private val Mint = Color(0xFF8BE8C1)
private val Amber = Color(0xFFE9BE6A)

@Composable
fun HomeScreen(
    context: Context,
    active: Boolean,
    mode: DimMode,
    depth: Int,
    accessibilityEnabled: Boolean,
    canDraw: Boolean,
    onToggle: () -> Unit,
    onMode: (DimMode) -> Unit,
    onDepth: (Int) -> Unit,
    onAccessibilityRefresh: () -> Unit
) {
    var showAccessibility by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().background(Ink).padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("暗幕", color = MaterialTheme.colorScheme.onBackground, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("让屏幕比系统最低亮度更暗", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            IconButton(onClick = { showAccessibility = true; onAccessibilityRefresh() }) {
                Icon(
                    imageVector = if (accessibilityEnabled) Icons.Filled.AccessibilityNew else Icons.Filled.WarningAmber,
                    contentDescription = "无障碍覆盖状态",
                    tint = if (accessibilityEnabled) Mint else Amber
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(if (active) "暗影守卫" else "光能守卫", color = MaterialTheme.colorScheme.onBackground, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
        Text(if (active) "暗幕覆盖正在守护屏幕" else "准备好降低屏幕亮度", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(18.dp))
        CoreSwitch(active = active, onClick = onToggle)
        Spacer(Modifier.height(18.dp))
        Text(if (active) "覆盖已开启" else "覆盖已关闭", color = if (active) Mint else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))
        ModeRow(mode = mode, onMode = onMode)
        Spacer(Modifier.height(22.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Panel).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("遮罩深度", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                Text("$depth%", color = Mint, fontWeight = FontWeight.Bold)
            }
            Slider(value = depth.toFloat(), onValueChange = { onDepth(it.toInt()) }, valueRange = 0f..90f, colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Mint, activeTrackColor = Mint, inactiveTrackColor = Color(0xFF30433F)))
            if (depth >= 80) Text("深度较高，请确认仍能看清屏幕", color = Amber, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))
        StatusCard(accessibilityEnabled, canDraw)
    }
    if (showAccessibility) {
        AlertDialog(
            onDismissRequest = { showAccessibility = false },
            containerColor = Panel,
            title = { Text("无障碍覆盖", color = MaterialTheme.colorScheme.onBackground) },
            text = { Text(if (accessibilityEnabled) "无障碍全屏覆盖已开启\n\n仅用于在屏幕上显示护眼遮罩，不读取屏幕内容、不执行点击、不控制其他应用。" else "未开启无障碍覆盖。普通悬浮窗仍可使用；开启后可获得更完整的覆盖范围。", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); showAccessibility = false }) { Text("去开启") } },
            dismissButton = { TextButton(onClick = { showAccessibility = false }) { Text("关闭") } }
        )
    }
}

@Composable
private fun CoreSwitch(active: Boolean, onClick: () -> Unit) {
    Box(Modifier.size(166.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * .42f
            drawCircle(Brush.radialGradient(listOf(if (active) Color(0xFF1B4B3B) else Color(0xFF263D39), Color(0xFF0E1717))), radius = radius)
            drawCircle(if (active) Mint else Color(0xFF6C9285), radius = radius, style = Stroke(width = 5.dp.toPx()))
            drawCircle(if (active) Color(0xFFB7FFE1) else Color(0xFFB5D6CA), radius = radius * .22f)
            drawLine(if (active) Mint else Color(0xFF86AFA1), Offset(center.x, center.y - radius * .76f), Offset(center.x, center.y - radius * .42f), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
        }
        Icon(Icons.Filled.BrightnessLow, contentDescription = "开启或关闭遮罩", tint = if (active) Mint else Color(0xFFB5D6CA), modifier = Modifier.size(34.dp))
    }
}

@Composable
private fun ModeRow(mode: DimMode, onMode: (DimMode) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DimMode.entries.forEach { item ->
            val selected = item == mode
            Box(Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(10.dp)).background(if (selected) Color(0xFF1D3C35) else Panel).border(1.dp, if (selected) Mint else Color(0xFF30433F), RoundedCornerShape(10.dp)).clickable { onMode(item) }, contentAlignment = Alignment.Center) {
                Text(item.label, color = if (selected) Mint else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun StatusCard(accessibilityEnabled: Boolean, canDraw: Boolean) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Panel).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(if (accessibilityEnabled) Icons.Filled.Check else Icons.Filled.BrightnessLow, contentDescription = null, tint = if (accessibilityEnabled) Mint else Amber, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(if (accessibilityEnabled) "无障碍全屏覆盖" else "普通悬浮窗覆盖", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
            Text(if (canDraw) "覆盖权限已准备" else "开启时将请求悬浮窗权限", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}
