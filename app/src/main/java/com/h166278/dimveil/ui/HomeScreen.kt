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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h166278.dimveil.BuildConfig
import com.h166278.dimveil.domain.DimMode
import com.h166278.dimveil.overlay.OverlayError
import com.h166278.dimveil.overlay.OverlayHostKind

// —— 暗幕色板 ——
private val Ink = Color(0xFF070B0D)
private val Panel = Color(0xFF101719)
private val PillBg = Color(0xFF0C1214)
private val PanelBorder = Color(0xFF24342F)
private val Mint = Color(0xFF8BE8C1)
private val MintBright = Color(0xFFB7FFE1)
private val Amber = Color(0xFFE9BE6A)
private val MutedTeal = Color(0xFF6C9285)
private val TrackInactive = Color(0xFF30433F)
private val ModeSelectedBg = Color(0xFF1D3C35)

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
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandHeader()
            Spacer(Modifier.height(32.dp))
            GuardTitle(active = active, blocked = !state.canStart)
            Spacer(Modifier.height(20.dp))
            CoreSwitch(active = active, enabled = state.canStart || active, onClick = onToggle)
            Spacer(Modifier.height(14.dp))
            OverlayStateLabel(state = state)
            Spacer(Modifier.height(26.dp))
            ModeRow(mode = mode, onMode = onMode)
            Spacer(Modifier.height(14.dp))
            DepthCard(
                depth = depth,
                onDepthPreview = onDepthPreview,
                onDepthCommit = onDepthCommit
            )
            Spacer(Modifier.height(14.dp))
            StatusCard(
                state = state,
                onOpenAccessibility = { showAccessibility = true }
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "暗幕 v${BuildConfig.VERSION_NAME} · 离线无追踪",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
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
                    else "未开启无障碍覆盖，开启后可获得更完整的覆盖范围",
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
private fun BrandHeader() {
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
        Spacer(Modifier.width(42.dp))
    }
}

@Composable
private fun GuardTitle(active: Boolean, blocked: Boolean) {
    val title = when {
        blocked -> "需要授权"
        active -> "遮罩正在运行"
        else -> "降低屏幕亮度"
    }
    val detail = when {
        blocked -> "授予覆盖权限后即可开启"
        active -> "轻触核心即可关闭"
        else -> "开启后可低于系统最低亮度"
    }
    Crossfade(targetState = title to detail, label = "guardTitle") { (headline, supporting) ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                headline,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                supporting,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun OverlayStateLabel(state: MainUiState) {
    val active = state.active && state.host != null
    val color = when {
        active -> Mint
        state.canStart -> MutedTeal
        else -> Amber
    }
    val label = when {
        state.host == OverlayHostKind.ACCESSIBILITY && active -> "当前遮罩：无障碍遮罩"
        state.host == OverlayHostKind.NORMAL && active -> "当前遮罩：悬浮窗遮罩"
        state.accessibilityEnabled -> "无障碍遮罩（待开启）"
        state.canDraw -> "悬浮窗遮罩（待开启）"
        else -> "需要悬浮窗或无障碍权限"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CoreSwitch(active: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val ringColor by animateColorAsState(
        when {
            active -> Mint
            enabled -> MutedTeal
            else -> Amber.copy(alpha = 0.72f)
        },
        label = "ring"
    )
    val coreColor by animateColorAsState(
        if (active) MintBright else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "core"
    )
    val fillTop by animateColorAsState(
        if (active) Color(0xFF17372E) else Color(0xFF131D1E),
        label = "fillTop"
    )
    val glowAlpha by animateFloatAsState(if (active) 0.72f else 0f, label = "glow")
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "scale")

    Box(
        Modifier
            .size(188.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .semantics {
                role = Role.Switch
                stateDescription = when {
                    active -> "遮罩已开启"
                    enabled -> "遮罩已关闭"
                    else -> "需要权限才能开启"
                }
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 外层辉光
        Box(
            Modifier.size(172.dp).background(
                Brush.radialGradient(listOf(ringColor.copy(alpha = 0.30f * glowAlpha), Color.Transparent)),
                CircleShape
            )
        )
        Canvas(Modifier.size(150.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.44f
            // 盘面渐变
            drawCircle(Brush.radialGradient(listOf(fillTop, Color(0xFF0E1717))), radius = radius, center = center)
            // 主环
            drawCircle(ringColor, radius = radius, center = center, style = Stroke(width = 5.dp.toPx()))
            // 外圈淡环
            drawCircle(ringColor.copy(alpha = 0.35f), radius = radius + 10.dp.toPx(), center = center, style = Stroke(width = 2.dp.toPx()))
            // 核心亮点
            drawCircle(coreColor, radius = radius * 0.22f, center = center)
            // 顶部指示灯
            drawLine(ringColor, Offset(center.x, center.y - radius * 0.78f), Offset(center.x, center.y - radius * 0.42f), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
        }
        Icon(Icons.Filled.BrightnessLow, contentDescription = "开启或关闭遮罩", tint = coreColor, modifier = Modifier.size(32.dp))
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
            colors = SliderDefaults.colors(thumbColor = Mint, activeTrackColor = Mint, inactiveTrackColor = TrackInactive)
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
private fun StatusCard(
    state: MainUiState,
    onOpenAccessibility: () -> Unit
) {
    val accessibilityMissing = !state.accessibilityEnabled
    val accent = if (accessibilityMissing) MutedTeal else Mint
    val title = if (accessibilityMissing) "未开启无障碍覆盖" else "无障碍覆盖已开启"
    val detail = when {
        accessibilityMissing -> "开启后可获得更完整的覆盖范围"
        state.error == OverlayError.WINDOW_REJECTED -> "系统拒绝创建遮罩，请重新授权"
        state.error == OverlayError.FOREGROUND_START_FAILED -> "前台服务启动失败，请重试"
        state.depthLimited -> "普通覆盖已安全限制为 ${state.appliedDepth}%"
        !state.notificationsAllowed -> "通知权限未开启，请从应用内关闭遮罩"
        else -> "可获得更完整的覆盖范围"
    }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Panel)
            .border(1.dp, PanelBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenAccessibility)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ModeSelectedBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
        }
    }
}
