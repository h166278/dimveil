package com.h166278.dimveil.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h166278.dimveil.BuildConfig
import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.overlay.OverlayError
import com.h166278.dimveil.overlay.OverlayHostKind

// Reference-inspired energy palette. Labels and behavior remain product-owned.
private val Ink = Color(0xFF090917)
private val Panel = Color(0xFF111223)
private val PillBg = Color(0xFF111323)
private val PanelBorder = Color(0xFF292741)
private val Violet = Color(0xFFC19AFF)
private val VioletBright = Color(0xFFE1CFFF)
private val Cyan = Color(0xFF68D4D5)
private val Mint = Color(0xFF6CF0BD)
private val MintBright = Color(0xFFB8FFE5)
private val Amber = Color(0xFFFFC86B)
private val MutedTeal = Color(0xFF7D75AE)
private val TrackInactive = Color(0xFF303046)
private val ModeSelectedBg = Color(0xFF33215D)
private val StatusWarnBg = Color(0xFF3A3025)

@Composable
fun HomeScreen(
    state: MainUiState,
    onToggle: () -> Unit,
    onMode: (DimMode) -> Unit,
    onDepthPreview: (Int) -> Unit,
    onDepthCommit: () -> Unit,
    onOpenAccessibility: () -> Unit
) {
    var showAccessibility by remember { mutableStateOf(false) }
    val active = state.active
    val mode = state.mode
    val depth = state.depth
    val accessibilityEnabled = state.accessibilityEnabled

    Box(Modifier.fillMaxSize().background(Ink)) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .size(520.dp)
                .offset(y = (-240).dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF2B1A55).copy(alpha = 0.72f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .size(260.dp)
                .offset(x = 100.dp, y = 180.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF0D4C4A).copy(alpha = 0.28f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandHeader(accessibilityEnabled = accessibilityEnabled, onClick = { showAccessibility = true })
            Spacer(Modifier.height(46.dp))
            GuardTitle(active = active)
            Spacer(Modifier.height(16.dp))
            CoreSwitch(active = active, onClick = onToggle)
            Spacer(Modifier.height(16.dp))
            Crossfade(targetState = active, label = "overlayState") { on ->
                Text(
                    if (on) "覆盖已开启" else "覆盖已关闭",
                    color = if (on) Mint else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(24.dp))
            ModeRow(mode = mode, onMode = onMode)
            Spacer(Modifier.height(16.dp))
            DepthCard(depth = depth, onDepthPreview = onDepthPreview, onDepthCommit = onDepthCommit)
            Spacer(Modifier.height(16.dp))
            StatusCard(state = state)
            Spacer(Modifier.height(22.dp))
            Text(
                "暗幕 v${BuildConfig.VERSION_NAME} · 离线无追踪",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }
    }

    if (showAccessibility) {
        AlertDialog(
            onDismissRequest = { showAccessibility = false },
            containerColor = Panel,
            title = { Text("无障碍覆盖", color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Text(
                    if (accessibilityEnabled) "无障碍全屏覆盖已开启\n\n仅用于在屏幕上显示护眼遮罩，不读取屏幕内容、不执行点击、不控制其他应用。"
                    else "未开启无障碍覆盖。普通悬浮窗仍可使用；开启后可获得更完整的覆盖范围。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { onOpenAccessibility(); showAccessibility = false }) {
                    Text(if (accessibilityEnabled) "系统设置" else "去开启")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibility = false }) { Text("关闭") }
            }
        )
    }
}

@Composable
private fun BrandHeader(accessibilityEnabled: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 能量核心徽标
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF1D3C35), Color(0xFF0E1717))))
                    .border(1.dp, Color(0xFF2A4A40), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(22.dp)) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val r = size.minDimension * 0.40f
                    val diamond = Path().apply {
                        moveTo(c.x, c.y - r)
                        lineTo(c.x + r, c.y)
                        lineTo(c.x, c.y + r)
                        lineTo(c.x - r, c.y)
                        close()
                    }
                    drawPath(diamond, Mint.copy(alpha = 0.95f), style = Stroke(width = 2.5.dp.toPx()))
                    drawCircle(Mint.copy(alpha = 0.9f), radius = r * 0.28f, center = c)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("暗幕", color = MaterialTheme.colorScheme.onBackground, fontSize = 25.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Text("DIM VEIL", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, letterSpacing = 3.sp)
            }
        }
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (accessibilityEnabled) Icons.Filled.AccessibilityNew else Icons.Filled.WarningAmber,
                contentDescription = "无障碍覆盖状态",
                tint = if (accessibilityEnabled) Mint else Amber
            )
        }
    }
}

@Composable
private fun GuardTitle(active: Boolean) {
    Crossfade(targetState = active, label = "guardTitle") { on ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (on) "暗影守卫" else "光能守卫",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (on) "暗幕覆盖正在守护屏幕" else "准备好降低屏幕亮度",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun CoreSwitch(active: Boolean, onClick: () -> Unit) {
    val coreColor by animateColorAsState(if (active) VioletBright else Color(0xFFC5B8D9), label = "core")
    val glowAlpha by animateFloatAsState(if (active) 1f else 0.34f, label = "glow")
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "scale")

    Box(
        Modifier
            .size(292.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .semantics {
                role = Role.Switch
                stateDescription = if (active) "遮罩已开启" else "遮罩已关闭"
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(288.dp).background(
                Brush.radialGradient(
                    listOf(Violet.copy(alpha = 0.20f * glowAlpha), Cyan.copy(alpha = 0.08f * glowAlpha), Color.Transparent)
                ),
                CircleShape
            )
        )
        Canvas(Modifier.size(274.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.minDimension * 0.47f
            val innerRadius = size.minDimension * 0.34f
            drawCircle(Color(0xFF111125), radius = outerRadius, center = center)
            drawCircle(
                Brush.sweepGradient(listOf(Cyan, Violet, Color(0xFF8A6CFF), Mint, Cyan)),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 5.dp.toPx())
            )
            drawCircle(Violet.copy(alpha = 0.18f), radius = outerRadius - 9.dp.toPx(), center = center)
            drawCircle(
                Brush.radialGradient(listOf(Color(0xFF302258), Color(0xFF1A1933))),
                radius = innerRadius,
                center = center
            )
            drawCircle(Violet.copy(alpha = 0.45f), radius = innerRadius, center = center, style = Stroke(width = 1.dp.toPx()))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.BrightnessLow, contentDescription = "开启或关闭遮罩", tint = coreColor, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(14.dp))
            Text(if (active) "覆盖已开启" else "覆盖已关闭", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ModeRow(mode: DimMode, onMode: (DimMode) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DimMode.entries.forEach { item ->
            val selected = item == mode
            val bg by animateColorAsState(if (selected) ModeSelectedBg else PillBg, label = "modeBg")
            val borderColor by animateColorAsState(if (selected) Mint.copy(alpha = 0.7f) else PanelBorder, label = "modeBorder")
            val fg by animateColorAsState(if (selected) Mint else MaterialTheme.colorScheme.onSurfaceVariant, label = "modeFg")
            Column(
                Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg)
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .semantics {
                        this.selected = selected
                        role = Role.RadioButton
                    }
                    .clickable { onMode(item) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(modeIcon(item), contentDescription = null, tint = fg, modifier = Modifier.size(19.dp))
                Spacer(Modifier.height(4.dp))
                Text(item.label, color = fg, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

private fun modeIcon(mode: DimMode): ImageVector = when (mode) {
    DimMode.NIGHT -> Icons.Filled.DarkMode
    DimMode.READING -> Icons.Filled.MenuBook
    DimMode.GAME -> Icons.Filled.SportsEsports
    DimMode.CUSTOM -> Icons.Filled.Tune
}

@Composable
private fun DepthCard(
    depth: Int,
    onDepthPreview: (Int) -> Unit,
    onDepthCommit: () -> Unit
) {
    val animatedDepth by animateIntAsState(depth, label = "depth")
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Panel)
            .border(1.dp, PanelBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("遮罩深度", color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text("调节暗幕浓度 0–90%", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$animatedDepth", color = Mint, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text(
                    "%",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 2.dp, bottom = 5.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Slider(
            value = depth.toFloat(),
            onValueChange = { onDepthPreview(it.toInt()) },
            onValueChangeFinished = onDepthCommit,
            valueRange = 0f..90f,
            colors = SliderDefaults.colors(
                thumbColor = Violet,
                activeTrackColor = Violet,
                inactiveTrackColor = TrackInactive
            )
        )
        if (depth >= 80) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = Amber, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("深度较高，请确认仍能看清屏幕", color = Amber, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StatusCard(state: MainUiState) {
    val healthy = state.error == null && (state.active || state.canStart)
    val title = when (state.host) {
        OverlayHostKind.ACCESSIBILITY -> "无障碍全屏覆盖运行中"
        OverlayHostKind.NORMAL -> "普通悬浮窗覆盖运行中"
        null -> if (state.accessibilityReady) "无障碍覆盖已准备" else "普通悬浮窗覆盖"
    }
    val detail = when {
        state.error == OverlayError.NO_AVAILABLE_HOST -> "没有可用的覆盖权限"
        state.error == OverlayError.WINDOW_REJECTED -> "系统拒绝创建遮罩，请重新授权"
        state.error == OverlayError.FOREGROUND_START_FAILED -> "前台服务启动失败，请重试"
        state.depthLimited -> "普通覆盖已安全限制为 ${state.appliedDepth}%"
        !state.notificationsAllowed -> "通知权限未开启，请从应用内关闭遮罩"
        state.accessibilityEnabled && !state.accessibilityReady -> "无障碍服务正在连接"
        state.canDraw || state.accessibilityReady -> "覆盖权限已准备"
        else -> "开启时将请求悬浮窗权限"
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (healthy) Color(0xFF102B2A) else Panel)
            .border(1.dp, if (healthy) Mint.copy(alpha = 0.35f) else PanelBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (healthy) Color(0xFF195445) else StatusWarnBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (healthy) Icons.Filled.Verified else Icons.Filled.Shield,
                contentDescription = null,
                tint = if (healthy) Mint else Amber,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (healthy) Mint else Amber))
    }
}
