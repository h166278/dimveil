package com.h166278.dimveil.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
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

private val Canvas = Color(0xFF202124)
private val Panel = Color(0xFF2C2D31)
private val PanelRaised = Color(0xFF37383D)
private val Stroke = Color(0xFF4A4B51)
private val Ink = Color(0xFFF5F5F6)
private val Muted = Color(0xFFB7B9C2)
private val Blue = Color(0xFF18A0FB)
private val BlueContainer = Color(0xFF163A55)
private val Green = Color(0xFF55C2A5)
private val Amber = Color(0xFFFFC76A)
private val Coral = Color(0xFFFF8A80)
private val Corner = RoundedCornerShape(6.dp)

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
    Box(Modifier.fillMaxSize().background(Canvas)) {
        Column(Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState())) {
            AppHeader(state.accessibilityEnabled) { showAccessibility = true }
            Workspace(state, onToggle, onMode, onDepthPreview, onDepthCommit)
            Footer()
        }
    }
    if (showAccessibility) AccessibilityDialog(state.accessibilityEnabled, onOpenAccessibility) { showAccessibility = false }
}

@Composable
private fun AppHeader(accessibilityEnabled: Boolean, onAccessibility: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(62.dp).background(Color(0xFF292A2E))
            .border(1.dp, Stroke).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(28.dp).clip(Corner).background(Blue), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.DarkMode, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("暗幕", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("DIM VEIL / SCREEN OVERLAY", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
        }
        StatusChip(accessibilityEnabled)
        Spacer(Modifier.width(6.dp))
        IconButton(onClick = onAccessibility) {
            Icon(Icons.Filled.AccessibilityNew, "无障碍覆盖设置", tint = if (accessibilityEnabled) Green else Amber)
        }
    }
}

@Composable
private fun StatusChip(ready: Boolean) {
    Row(
        Modifier.clip(Corner).background(if (ready) Color(0xFF203C37) else Color(0xFF493C22))
            .padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(if (ready) Green else Amber))
        Spacer(Modifier.width(6.dp))
        Text(if (ready) "覆盖已就绪" else "等待授权", color = if (ready) Green else Amber, fontSize = 11.sp)
    }
}

@Composable
private fun Workspace(
    state: MainUiState,
    onToggle: () -> Unit,
    onMode: (DimMode) -> Unit,
    onDepthPreview: (Int) -> Unit,
    onDepthCommit: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PreviewCanvas(state.active, state.depth, onToggle)
        InspectorHeader("效果属性", "PROPERTIES")
        ModeInspector(state.mode, onMode)
        DepthInspector(state.depth, onDepthPreview, onDepthCommit)
        RuntimeInspector(state)
    }
}

@Composable
private fun PreviewCanvas(active: Boolean, depth: Int, onToggle: () -> Unit) {
    val surface by animateColorAsState(if (active) Color(0xFF101114) else Color(0xFF25262A), label = "previewSurface")
    Column(
        Modifier.fillMaxWidth().clip(Corner).background(surface).border(1.dp, Stroke, Corner)
            .padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("PREVIEW", color = Muted, fontSize = 10.sp, letterSpacing = 1.2.sp)
            Text(if (active) "ACTIVE" else "IDLE", color = if (active) Green else Muted, fontSize = 10.sp, letterSpacing = 1.2.sp)
        }
        Spacer(Modifier.height(24.dp))
        ToggleControl(active, onToggle)
        Spacer(Modifier.height(18.dp))
        Text(if (active) "暗幕正在保护你的屏幕" else "准备好降低屏幕亮度", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(if (active) "当前遮罩深度 $depth%" else "点击中央开关，即刻启用暗幕覆盖", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("画布遮罩", color = Muted, fontSize = 11.sp)
            Text("${if (active) depth else 0}%", color = Blue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ToggleControl(active: Boolean, onToggle: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .94f else 1f, label = "toggleScale")
    val color by animateColorAsState(if (active) Blue else PanelRaised, label = "toggleColor")
    Box(
        Modifier.size(104.dp).graphicsLayer { scaleX = scale; scaleY = scale }.clip(CircleShape)
            .background(color).border(1.dp, if (active) Color(0xFF87D4FF) else Stroke, CircleShape)
            .semantics { role = Role.Switch; stateDescription = if (active) "遮罩已开启" else "遮罩已关闭" }
            .clickable(source, null, onClick = onToggle), contentAlignment = Alignment.Center
    ) { Icon(Icons.Filled.Bolt, "切换暗幕", tint = if (active) Color.White else Muted, modifier = Modifier.size(40.dp)) }
}

@Composable
private fun InspectorHeader(title: String, meta: String) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text(meta, color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun ModeInspector(mode: DimMode, onMode: (DimMode) -> Unit) {
    Column(Modifier.fillMaxWidth().clip(Corner).background(Panel).border(1.dp, Stroke, Corner).padding(12.dp)) {
        Label("预设", "选择遮罩模式")
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DimMode.entries.forEach { item ->
                val chosen = item == mode
                Column(
                    Modifier.weight(1f).clip(Corner).background(if (chosen) BlueContainer else PanelRaised)
                        .border(1.dp, if (chosen) Blue else Color.Transparent, Corner).padding(vertical = 9.dp)
                        .semantics { selected = chosen; role = Role.RadioButton }.clickable { onMode(item) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(modeIcon(item), null, tint = if (chosen) Blue else Muted, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.height(5.dp))
                    Text(item.label, color = if (chosen) Ink else Muted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun DepthInspector(depth: Int, onDepthPreview: (Int) -> Unit, onDepthCommit: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(Corner).background(Panel).border(1.dp, Stroke, Corner).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Label("遮罩深度", "0% - 90%")
            Text("$depth%", color = Blue, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        }
        Slider(value = depth.toFloat(), onValueChange = { onDepthPreview(it.toInt()) }, onValueChangeFinished = onDepthCommit,
            valueRange = 0f..90f, colors = SliderDefaults.colors(thumbColor = Ink, activeTrackColor = Blue, inactiveTrackColor = Stroke))
        if (depth >= 80) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WarningAmber, null, tint = Amber, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp)); Text("高强度遮罩，请保留必要可见度", color = Amber, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun RuntimeInspector(state: MainUiState) {
    val healthy = state.error == null && (state.active || state.canStart)
    val title = when (state.host) {
        OverlayHostKind.ACCESSIBILITY -> "无障碍全屏覆盖"
        OverlayHostKind.NORMAL -> "普通悬浮窗覆盖"
        null -> "覆盖运行状态"
    }
    val detail = when {
        state.error == OverlayError.NO_AVAILABLE_HOST -> "没有可用的覆盖权限"
        state.error == OverlayError.WINDOW_REJECTED -> "系统拒绝创建遮罩，请重新授权"
        state.error == OverlayError.FOREGROUND_START_FAILED -> "前台服务启动失败，请重试"
        state.depthLimited -> "普通覆盖已限制为 ${state.appliedDepth}%"
        state.active -> "遮罩已应用，点击预览区中央开关可关闭"
        state.canDraw || state.accessibilityReady -> "权限已准备，随时可以启用"
        else -> "启用时将请求覆盖权限"
    }
    Row(Modifier.fillMaxWidth().clip(Corner).background(Panel).border(1.dp, Stroke, Corner).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(30.dp).clip(Corner).background(if (healthy) Color(0xFF203C37) else Color(0xFF493C22)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Shield, null, tint = if (healthy) Green else Amber, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) { Label(title, detail) }
        Box(Modifier.size(7.dp).clip(CircleShape).background(if (healthy) Green else Coral))
    }
}

@Composable
private fun Label(title: String, detail: String) {
    Text(title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    Spacer(Modifier.height(2.dp)); Text(detail, color = Muted, fontSize = 11.sp)
}

private fun modeIcon(mode: DimMode): ImageVector = when (mode) {
    DimMode.NIGHT -> Icons.Filled.DarkMode
    DimMode.READING -> Icons.Filled.MenuBook
    DimMode.GAME -> Icons.Filled.SportsEsports
    DimMode.CUSTOM -> Icons.Filled.Tune
}

@Composable
private fun Footer() {
    Text("暗幕 v${BuildConfig.VERSION_NAME}  |  本地运行，无追踪", color = Muted, fontSize = 10.sp,
        modifier = Modifier.fillMaxWidth().padding(20.dp), fontWeight = FontWeight.Medium)
}

@Composable
private fun AccessibilityDialog(enabled: Boolean, onOpen: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, shape = Corner, containerColor = Panel, titleContentColor = Ink, textContentColor = Muted,
        title = { Text("无障碍覆盖", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
        text = { Text(if (enabled) "无障碍全屏覆盖已开启，仅用于显示护眼遮罩。" else "普通悬浮窗仍可使用。开启无障碍覆盖可获得更完整的覆盖范围。", fontSize = 13.sp) },
        confirmButton = { TextButton(onClick = { onOpen(); onDismiss() }) { Text(if (enabled) "系统设置" else "去开启", color = Blue) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭", color = Muted) } }
    )
}
