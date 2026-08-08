# 规格：遮罩状态重组（Overlay Status Recomposition）

> 冷读者须知：本文描述暗幕（com.h166278.dimveil）主界面的遮罩状态表达重组。前置概念：
> `host` = 当前实际承载遮罩的宿主，取值 `OverlayHostKind.NORMAL`（普通悬浮窗）或 `OverlayHostKind.ACCESSIBILITY`（无障碍服务覆盖）。
> `canDraw` = 已授予悬浮窗权限；`accessibilityEnabled` = 已启用无障碍服务；`active` = OverlayRuntime 报告遮罩已挂载。

## 1. 目标

重组主界面遮罩状态表达，达到：

1. 无障碍提示与操作入口改由底部状态卡承担（点击弹窗，弹窗内提供「关闭 / 去开启」）。
2. 右上角不再显示任何状态图标或交互。
3. 主开关下方实时显示当前遮罩类型；未运行时显示按可用权限推导的「待开启」文案。
4. 悬浮窗权限入口不丢失（阻塞态点主开关回退跳转系统授权页）。

## 2. 状态契约

### 2.1 顶部区域（BrandHeader）

- 仅渲染品牌徽标 + 应用名。
- 右侧不渲染任何图标、按钮或占位交互；所有状态下均为空。
- `BrandHeader()` 无参数（移除 `accessibilityEnabled`、`onClick`）。

### 2.2 主开关下方标签（OverlayStateLabel）

输入改为 `state: MainUiState`。按优先级首个命中：

| # | 条件 | 文案 | 颜色 |
|---|------|------|------|
| 1 | `active && host == ACCESSIBILITY` | 当前遮罩：无障碍遮罩 | Mint |
| 2 | `active && host == NORMAL` | 当前遮罩：悬浮窗遮罩 | Mint |
| 3 | 未运行 且 `accessibilityEnabled` | 无障碍遮罩（待开启） | MutedTeal（canStart 时） |
| 4 | 未运行 且 `canDraw` | 悬浮窗遮罩（待开启） | MutedTeal（canStart 时） |
| 5 | 未运行 且均不可用 | 需要悬浮窗或无障碍权限 | Amber |

颜色逻辑整体为：`active → Mint`；`canStart → MutedTeal`；否则 `Amber`。因此瞬态下（无障碍服务已启用但未连接、且无悬浮窗权限，即 `accessibilityEnabled && !accessibilityReady && !canDraw`），文案为「无障碍遮罩（待开启）」但颜色为 Amber（尚不可启动）。

注意：`active` 判定为 `state.active && state.host != null`，与 MainUiState.canStart（`accessibilityReady || canDraw`）解耦。

### 2.3 状态卡（StatusCard）

职责：无障碍状态主表达 + 无障碍开启后的运行提示副文案。**整卡可点击** → 打开「无障碍覆盖」弹窗（复用现有 AlertDialog）。卡片上不直接渲染「关闭 / 去开启」按钮。

**无障碍未开启（`!accessibilityEnabled`）：**
- 标题：未开启无障碍覆盖
- 副文案：开启后可获得更完整的覆盖范围（固定，不混入其他运行提示）
- 图标：`Icons.Filled.Verified`，tint MutedTeal；状态点 MutedTeal；边框 PanelBorder（无琥珀警示——克制冷青体系下 Amber 仅保留给真阻塞态，而本卡此时非阻塞）

**无障碍已开启：**
- 标题：无障碍覆盖已开启
- 副文案按优先级首个命中：
  1. `error == WINDOW_REJECTED` → 系统拒绝创建遮罩，请重新授权
  2. `error == FOREGROUND_START_FAILED` → 前台服务启动失败，请重试
  3. `depthLimited` → 普通覆盖已安全限制为 `{appliedDepth}%`
  4. `!notificationsAllowed` → 通知权限未开启，请从应用内关闭遮罩
  5. 兜底 → 可获得更完整的覆盖范围
- 图标：`Icons.Filled.Verified`，tint Mint；状态点 Mint；边框 PanelBorder

### 2.4 无障碍覆盖弹窗（AlertDialog）

入口：点击状态卡（`showAccessibility` 状态位）。弹窗同时承担「关闭 / 去开启」。

**无障碍未开启：**
- 文案（精确）：未开启无障碍覆盖，开启后可获得更完整的覆盖范围
- 确认按钮：去开启 → `onOpenAccessibility()`（ACTION_ACCESSIBILITY_SETTINGS）后关弹窗
- 取消按钮：关闭 → 仅关闭弹窗

**无障碍已开启：**
- 文案（精确）：无障碍全屏覆盖已开启\n\n仅用于在屏幕上显示护眼遮罩，不读取屏幕内容、不执行点击、不控制其他应用。
- 确认按钮：系统设置 → `onOpenAccessibility()` 后关弹窗
- 取消按钮：关闭 → 仅关闭弹窗

### 2.5 悬浮窗权限入口

- `HomeScreen` 签名移除 `onOpenOverlayPermission` 参数。
- `MainActivity` 对应移除传参；`openOverlayPermission()` 私有方法**保留**，由 `onToggle` 的既有回退分支引用：遮罩未运行且 `toggleOverlay()` 返回 false 时——无障碍服务已启用则跳无障碍设置，否则 `openOverlayPermission()`。
- 可行性依据：`CoreSwitch` 的 clickable 无 `enabled` 门控，阻塞态点击仍能触发 `onToggle`。

## 3. 非目标

- 不改 OverlayRuntime / OverlayController / DimAccessibilityService / OverlayService / 权限检测逻辑。
- 不新增图标资源（复用 Verified / WarningAmber 既有图标）。
- 不动模式卡、深度卡的其余内容（深度卡 80%+ 的 Amber 警示保留）。

## 4. 验证方式

无本地 Android 编译环境（沙箱缺 SDK/NDK），验收走：
1. **静态断言**（本地可执行）：`git diff` 逐 hunk 审阅；grep 断言——HomeScreen.kt 无 `IconButton`/`AccessibilityNew`/`onOpenOverlayPermission` 引用；MainActivity.kt 无 `onOpenOverlayPermission` 参数但 `openOverlayPermission()` 仍存在且被引用；弹窗未开启文案精确等于第 2.4 节字符串。
2. **编译验收**：推送到 GitHub Actions 后由 CI 编译验证（lintDebug + assembleDebug，既有 workflow）。

## 5. 已接受的已知取舍

- 无障碍已开启但遮罩未运行且无任何警告时，副文案为「可获得更完整的覆盖范围」——作为标题「无障碍覆盖已开启」的续句，语义自洽。
- 右上角移除图标后，顶部左右视觉重量靠品牌徽标 + 留白占位（`Spacer(Modifier.width(42.dp))`）平衡。
