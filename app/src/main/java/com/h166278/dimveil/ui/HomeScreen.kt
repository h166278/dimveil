package com.h166278.dimveil.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.h166278.dimveil.domain.DimMode

private val Ink = Color(0xFF070B0D)
private val Panel = Color(0xFF101719)
private val PanelSelected = Color(0xFF17322B)
private val PanelTrack = Color(0xFF30433F)
private val Mint = Color(0xFF8BE8C1)
private val MintSoft = Color(0xFFB7FFE1)
private val Amber = Color(0xFFE9BE6A)
private val PanelShape = RoundedCornerShape(8.dp)
private val SegmentShape = RoundedCornerShape(8.dp)

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
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .verticalScroll(scrollState)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header(
            accessibilityEnabled = accessibilityEnabled,
            onAccessibilityClick = {
                onAccessibilityRefresh()
                showAccessibility = true
            }
        )
        Spacer(Modifier.height(26.dp))
        GuardianSection(active = active, onToggle = onToggle)
        Spacer(Modifier.height(24.dp))
        ModeRow(mode = mode, onMode = onMode)
        Spacer(Modifier.height(22.dp))
        DepthPanel(depth = depth, onDepth = onDepth)
        Spacer(Modifier.height(12.dp))
        StatusPanel(accessibilityEnabled = accessibilityEnabled, canDraw = canDraw)
        Spacer(Modifier.height(16.dp))
    }

    if (showAccessibility) {
        AlertDialog(
            onDismissRequest = { showAccessibility = false },
            containerColor = Panel,
            title = { Text("无障碍覆盖", color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Text(
                    text = if (accessibilityEnabled) {
                        "无障碍全屏覆盖已开启\n\n仅用于在屏幕上显示护眼遮罩，不读取屏幕内容、不执行点击、不控制其他应用。"
                    } else {
                        "未开启无障碍覆盖。普通悬浮窗仍可使用；开启后可获得更完整的覆盖范围。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        showAccessibility = false
                    }
                ) {
                    Text("去开启")
                }
            },
            dismissButton = { TextButton(onClick = { showAccessibility = false }) { Text("关闭") } }
        )
    }
}

@Composable
private fun Header(accessibilityEnabled: Boolean, onAccessibilityClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("暗幕", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                "让屏幕比系统最低亮度更暗",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(onClick = onAccessibilityClick) {
            Icon(
                imageVector = if (accessibilityEnabled) Icons.Filled.AccessibilityNew else Icons.Filled.WarningAmber,
                contentDescription = "无障碍覆盖状态",
                tint = if (accessibilityEnabled) Mint else Amber
            )
        }
    }
}

@Composable
private fun GuardianSection(active: Boolean, onToggle: () -> Unit) {
    Text(
        text = if (active) "暗影守卫" else "光能守卫",
        style = MaterialTheme.typography.titleLarge
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = if (active) "暗幕覆盖正在守护屏幕" else "准备好降低屏幕亮度",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(Modifier.height(20.dp))
    CoreSwitch(active = active, onClick = onToggle)
    Spacer(Modifier.height(16.dp))
    Text(
        text = if (active) "覆盖已开启" else "覆盖已关闭",
        color = if (active) Mint else MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun CoreSwitch(active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(196.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.42f
            val coreColor = if (active) Color(0xFF1B4B3B) else Color(0xFF263D39)
            drawCircle(
                brush = Brush.radialGradient(listOf(coreColor, Color(0xFF0E1717))),
                radius = radius
            )
            drawCircle(
                color = if (active) Mint else Color(0xFF6C9285),
                radius = radius,
                style = Stroke(width = 4.dp.toPx())
            )
            drawCircle(
                color = if (active) MintSoft else Color(0xFFB5D6CA),
                radius = radius * 0.22f
            )
            drawLine(
                color = if (active) Mint else Color(0xFF86AFA1),
                start = Offset(center.x, center.y - radius * 0.76f),
                end = Offset(center.x, center.y - radius * 0.42f),
                strokeWidth = 7.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        Icon(
            imageVector = Icons.Filled.BrightnessLow,
            contentDescription = if (active) "关闭遮罩" else "开启遮罩",
            tint = if (active) Mint else Color(0xFFB5D6CA),
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
private fun ModeRow(mode: DimMode, onMode: (DimMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DimMode.entries.forEach { item ->
            val selected = item == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(SegmentShape)
                    .background(if (selected) PanelSelected else Panel)
                    .border(1.dp, if (selected) Mint else PanelTrack, SegmentShape)
                    .clickable { onMode(item) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.label,
                    color = if (selected) Mint else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DepthPanel(depth: Int, onDepth: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PanelShape)
            .background(Panel)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("遮罩深度", style = MaterialTheme.typography.titleMedium)
            Text("$depth%", color = Mint, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(8.dp))
        Slider(
            value = depth.toFloat(),
            onValueChange = { onDepth(it.toInt()) },
            valueRange = 0f..90f,
            colors = SliderDefaults.colors(
                thumbColor = Mint,
                activeTrackColor = Mint,
                inactiveTrackColor = PanelTrack
            )
        )
        if (depth >= 80) {
            Text(
                "深度较高，请确认仍能看清屏幕",
                color = Amber,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StatusPanel(accessibilityEnabled: Boolean, canDraw: Boolean) {
    val title = if (accessibilityEnabled) "无障碍全屏覆盖" else "普通悬浮窗覆盖"
    val detail = when {
        accessibilityEnabled -> "无障碍覆盖已准备"
        canDraw -> "覆盖权限已准备"
        else -> "开启时将请求悬浮窗权限"
    }
    val icon = if (accessibilityEnabled) Icons.Filled.Check else Icons.Filled.BrightnessLow
    val tint = if (accessibilityEnabled || canDraw) Mint else Amber

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PanelShape)
            .background(Panel)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
