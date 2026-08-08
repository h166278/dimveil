package com.h166278.dimveil.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
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

// Apple iOS dark system palette
private val AppleBlack = Color(0xFF000000)
private val AppleCard = Color(0xFF1C1C1E)
private val AppleFill = Color(0xFF2C2C2E)
private val AppleFillSelected = Color(0xFF48484A)
private val AppleLabel = Color(0xFFFFFFFF)
private val AppleSecondary = Color(0xFF98989F)
private val AppleTertiary = Color(0xFF6E6E73)
private val AppleGreen = Color(0xFF30D158)
private val AppleBlue = Color(0xFF0A84FF)
private val AppleYellow = Color(0xFFFFD60A)

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

    Box(Modifier.fillMaxSize().background(AppleBlack)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(accessibilityEnabled = accessibilityEnabled, onClick = { showAccessibility = true })
            Spacer(Modifier.height(38.dp))
            CoreSwitch(active = active, onClick = onToggle)
            Spacer(Modifier.height(18.dp))
            GuardStatus(active = active)
            Spacer(Modifier.height(38.dp))
            ModeRow(mode = mode, onMode = onMode)
            Spacer(Modifier.height(34.dp))
            DepthCard(depth = depth, onDepthPreview = onDepthPreview, onDepthCommit = onDepthCommit)
            Spacer(Modifier.height(34.dp))
            StatusCard(state = state)
            Spacer(Modifier.height(30.dp))
            Text(
                "暗幕 v${BuildConfig.VERSION_NAME} · 离线无追踪",
                color = AppleTertiary,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showAccessibility) {
        AlertDialog(
            onDismissRequest = { showAccessibility = false },
            shape = RoundedCornerShape(14.dp),
            containerColor = AppleCard,
            titleContentColor = AppleLabel,
            textContentColor = AppleSecondary,
            title = { Text("无障碍覆盖", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    if (accessibilityEnabled) "无障碍全屏覆盖已开启\n\n仅用于在屏幕上显示护眼遮罩，不读取屏幕内容、不执行点击、不控制其他应用。"
                    else "未开启无障碍覆盖。普通悬浮窗仍可使用；开启后可获得更完整的覆盖范围。",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { onOpenAccessibility(); showAccessibility = false }) {
                    Text(
                        if (accessibilityEnabled) "系统设置" else "去开启",
                        color = AppleBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibility = false }) { Text("关闭", color = AppleSecondary) }
            }
        )
    }
}

@Composable
private fun TopBar(accessibilityEnabled: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "暗幕",
                color = AppleLabel,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                "DIM VEIL",
                color = AppleSecondary,
                fontSize = 10.sp,
                letterSpacing = 4.sp
            )
        }
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (accessibilityEnabled) Icons.Filled.AccessibilityNew else Icons.Filled.WarningAmber,
                contentDescription = "无障碍覆盖状态",
                tint = if (accessibilityEnabled) AppleGreen else AppleYellow
            )
        }
    }
}

@Composable
private fun CoreSwitch(active: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (active) AppleGreen else AppleFill, label = "switchBg")
    val iconTint by animateColorAsState(if (active) AppleLabel else AppleSecondary, label = "switchIcon")
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "switchScale")

    Box(
        Modifier
            .size(152.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(bg)
            .semantics {
                role = Role.Switch
                stateDescription = if (active) "遮罩已开启" else "遮罩已关闭"
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.DarkMode,
            contentDescription = "开启或关闭遮罩",
            tint = iconTint,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
private fun GuardStatus(active: Boolean) {
    Crossfade(targetState = active, label = "guardStatus") { on ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (on) "覆盖已开启" else "覆盖已关闭",
                color = if (on) AppleLabel else AppleSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(3.dp))
            Text(
                if (on) "暗影守卫" else "光能守卫",
                color = AppleTertiary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ModeRow(mode: DimMode, onMode: (DimMode) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(AppleFill)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        DimMode.entries.forEach { item ->
            val selected = item == mode
            val bg by animateColorAsState(if (selected) AppleFillSelected else Color.Transparent, label = "segBg")
            val fg by animateColorAsState(if (selected) AppleLabel else AppleSecondary, label = "segFg")
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .semantics {
                        this.selected = selected
                        role = Role.RadioButton
                    }
                    .clickable { onMode(item) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(modeIcon(item), contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                Spacer(Modifier.height(2.dp))
                Text(
                    item.label,
                    color = fg,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
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
            .clip(RoundedCornerShape(14.dp))
            .background(AppleCard)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("遮罩深度", color = AppleLabel, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text("调节暗幕浓度 0–90%", color = AppleSecondary, fontSize = 13.sp)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$animatedDepth", color = AppleLabel, fontSize = 34.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "%",
                    color = AppleSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 2.dp, bottom = 5.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Slider(
            value = depth.toFloat(),
            onValueChange = { onDepthPreview(it.toInt()) },
            onValueChangeFinished = onDepthCommit,
            valueRange = 0f..90f,
            colors = SliderDefaults.colors(
                thumbColor = AppleLabel,
                activeTrackColor = AppleLabel,
                inactiveTrackColor = AppleFill
            )
        )
        if (depth >= 80) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = AppleYellow, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("深度较高，请确认仍能看清屏幕", color = AppleYellow, fontSize = 12.sp)
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
            .clip(RoundedCornerShape(14.dp))
            .background(AppleCard)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (healthy) AppleGreen else AppleYellow)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = AppleLabel,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                detail,
                color = AppleSecondary,
                fontSize = 13.sp
            )
        }
        Icon(
            if (healthy) Icons.Filled.Verified else Icons.Filled.Shield,
            contentDescription = null,
            tint = if (healthy) AppleGreen else AppleYellow,
            modifier = Modifier.size(18.dp)
        )
    }
}
