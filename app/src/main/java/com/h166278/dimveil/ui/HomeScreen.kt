package com.h166278.dimveil.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
// 与主题等值的颜色直接引用 MaterialTheme.colorScheme（primary/surface/tertiary 等），
// 避免硬编码与主题 token 漂移；以下仅为无对应 token 的自定义扩展色。
private val MutedTeal = Color(0xFF6C9285)
private val TrackInactive = Color(0xFF30433F)

@Composable
fun HomeScreen(
    state: MainUiState,
    onToggle: () -> Unit,
    onMode: (DimMode) -> Unit,
    onDepthPreview: (Int) -> Unit,
    onDepthCommit: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onDoubleTapAccessibility: () -> Unit
) {
    var showAccessibility by remember { mutableStateOf(false) }
    val active = state.active
    val mode = state.mode
    val depth = state.depth
    val accessibilityEnabled = state.accessibilityEnabled

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandHeader(
                accessibilityEnabled = accessibilityEnabled,
                onDoubleTapAccessibility = onDoubleTapAccessibility
            )
            Spacer(Modifier.height(22.dp))
            GuardTitle(active = active, blocked = !state.canStart)
            Spacer(Modifier.height(14.dp))
            CoreSwitch(active = active, enabled = state.canStart || active, onClick = onToggle)
            Spacer(Modifier.height(14.dp))
            OverlayStateLabel(state = state)
            Spacer(Modifier.height(20.dp))
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
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("无障碍权限", color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Text(
                    if (accessibilityEnabled) "无障碍权限已开启\n\n用于在屏幕上显示护眼遮罩，以获得更完整的遮罩范围；不读取屏幕内容、不执行点击、不控制其他应用。"
                    else "未开启无障碍权限。开启后可获得更完整的遮罩范围。\n\n此权限仅用于在屏幕上显示护眼遮罩，不读取屏幕内容、不执行点击、不控制其他应用。",
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
private fun BrandHeader(
    accessibilityEnabled: Boolean,
    onDoubleTapAccessibility: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 日全食徽标：白色月牙正被黑色天体遮住
            EclipseMark(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0xFF080D0F))
                    .border(1.dp, Color(0xFF2A4A40), RoundedCornerShape(13.dp))
                    .padding(8.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("暗幕", color = MaterialTheme.colorScheme.onBackground, fontSize = 25.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Text("DIM VEIL", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, letterSpacing = 3.sp)
            }
        }
        // 右上角无障碍快捷图标：双击直接经 Shizuku 开/关无障碍授权。
        // 单击不执行操作，避免与隐藏快捷方式产生误触。
        Box(
            Modifier
                .size(48.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { onDoubleTapAccessibility() }
                    )
                }
                .semantics {
                    role = Role.Button
                    stateDescription = if (accessibilityEnabled) "无障碍已开启" else "无障碍已关闭"
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AccessibilityNew,
                contentDescription = "无障碍快捷切换",
                tint = if (accessibilityEnabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EclipseMark(modifier: Modifier = Modifier) {
    val corona = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val center = Offset(size.width * 0.52f, size.height * 0.50f)
        val radius = size.minDimension * 0.42f
        // 日冕：只在黑色天体边缘露出一圈克制的冷青光
        drawCircle(
            color = corona.copy(alpha = 0.72f),
            radius = radius + 1.5.dp.toPx(),
            center = center,
            style = Stroke(width = 1.2.dp.toPx())
        )
        // 被遮住的白色月面，向左露出月牙
        drawCircle(color = Color.White, radius = radius, center = center.copy(x = center.x - radius * 0.28f))
        // 黑色天体向左压入，保留白色弯月与右侧日冕
        drawCircle(color = Color(0xFF080D0F), radius = radius * 0.86f, center = center)
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
        active -> MaterialTheme.colorScheme.primary
        state.canStart -> MutedTeal
        else -> MaterialTheme.colorScheme.tertiary
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
            active -> MaterialTheme.colorScheme.primary
            enabled -> MutedTeal
            else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.72f)
        },
        label = "ring"
    )
    val coreColor by animateColorAsState(
        if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "core"
    )
    val fillTop by animateColorAsState(
        if (active) Color(0xFF17372E) else Color(0xFF131D1E),
        label = "fillTop"
    )
    val glowAlpha by animateFloatAsState(if (active) 0.72f else 0f, label = "glow")
    val eclipseProgress by animateFloatAsState(if (active) 1f else 0f, label = "eclipseProgress")
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "scale")
    val pressOffset by animateFloatAsState(if (pressed) 0.16f else 0f, label = "pressOffset")

    Box(
        Modifier
            .size(172.dp)
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
            // 日食主视觉：关闭时月牙与黑色天体靠近，开启时完全重合
            val moonRadius = radius * 0.30f
            val moonCenter = center.copy(x = center.x - radius * 0.22f)
            val eclipseCenter = center.copy(
                x = moonCenter.x + radius * (0.44f * (1f - eclipseProgress) + 0.22f) - radius * pressOffset
            )
            drawCircle(Color.White.copy(alpha = 0.95f * (1f - eclipseProgress)), radius = moonRadius, center = moonCenter)
            drawCircle(Color(0xFF080D0F), radius = moonRadius * 0.96f, center = eclipseCenter)
            // 开启后只保留黑色天体边缘的冷青日冕
            drawCircle(
                ringColor.copy(alpha = 0.80f),
                radius = moonRadius * (0.96f + eclipseProgress * 0.05f),
                center = eclipseCenter,
                style = Stroke(width = 2.dp.toPx())
            )
        }

    }
}

@Composable
private fun ModeRow(mode: DimMode, onMode: (DimMode) -> Unit) {
    // M3 segmented-control 语义：一个统一容器，选中项使用 primaryContainer，
    // 避免四个独立描边卡片产生厚重、割裂的视觉。
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        DimMode.entries.forEach { item ->
            val selected = item == mode
            val bg by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                label = "modeBg"
            )
            val fg by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "modeFg"
            )
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .semantics {
                        this.selected = selected
                        role = Role.RadioButton
                    }
                    .clickable { onMode(item) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    modeIcon(item, selected),
                    contentDescription = item.label,
                    tint = fg,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    item.label,
                    color = fg,
                    style = if (selected) MaterialTheme.typography.labelLarge
                    else MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

private fun modeIcon(mode: DimMode, selected: Boolean): ImageVector = when (mode) {
    // 夜间保持同一方向：未选中空心月牙，选中实心月牙。
    DimMode.NIGHT -> if (selected) Icons.Filled.DarkMode else Icons.Outlined.DarkMode
    DimMode.READING -> if (selected) Icons.Filled.MenuBook else Icons.Outlined.MenuBook
    DimMode.GAME -> if (selected) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports
    DimMode.CUSTOM -> if (selected) Icons.Filled.Tune else Icons.Outlined.Tune
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
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
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
                Text("$animatedDepth", color = MaterialTheme.colorScheme.primary, fontSize = 34.sp, fontWeight = FontWeight.Bold)
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
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary, inactiveTrackColor = TrackInactive)
        )
        if (depth >= 80) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("深度较高，请确认仍能看清屏幕", color = MaterialTheme.colorScheme.tertiary, fontSize = 12.sp)
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
    val accent = if (accessibilityMissing) MutedTeal else MaterialTheme.colorScheme.primary
    val title = if (accessibilityMissing) "未开启无障碍权限" else "无障碍权限已开启"
    val detail = when {
        accessibilityMissing -> "开启后可获得更完整的遮罩范围"
        state.accessibilityEnabled && !state.accessibilityReady -> "无障碍服务连接中，稍候自动生效"
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
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium)
                Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onOpenAccessibility) {
                Text(
                    when {
                        accessibilityMissing -> "去开启"
                        state.error != null -> "查看设置"
                        else -> "已开启"
                    }
                )
            }
        }
    }
}
