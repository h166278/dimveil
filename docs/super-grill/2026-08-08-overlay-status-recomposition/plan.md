# 遮罩状态重组 Catering Plan

> Required cooking route: **Lonely Chef with Clipboard** — 本环境无可用副代理模型（Brigade 不可行）；改动限于单文件，静态断言可独立验证。

Original order: 重组暗幕主界面遮罩状态表达——无障碍提示/操作改由状态卡承担（点击弹窗，「关闭/去开启」在弹窗内）；右上角不显示任何图标；主开关下方显示当前遮罩类型（运行中）/待开启类型（未运行）；悬浮窗权限入口保留。
Architecture: 见 docs/super-grill/2026-08-08-overlay-status-recomposition/spec.md。工作树基线 = 用户已落地的未提交改动（BrandHeader 无图标、OverlayStateLabel 待开启文案、StatusCard 无障碍卡骨架、MainActivity 签名同步）。
Kitchen equipment: /tmp/dimveil.PeDeoO（main，HEAD 1a76c69 + 未提交基线改动）

## Restaurant-wide constraints

- 文案精确串（spec 2.4）：`未开启无障碍覆盖，开启后可获得更完整的覆盖范围`；`无障碍全屏覆盖已开启\n\n仅用于在屏幕上显示护眼遮罩，不读取屏幕内容、不执行点击、不控制其他应用。`
- 颜色语义（spec 2.2）：active→Mint；canStart→MutedTeal；否则 Amber。
- 状态卡无障碍已开启时副文案按 spec 2.3 优先级链。
- 不新增图标资源、不新增/删除 MainActivity 权限入口方法、不引入未批准行为。
- 每串 Sizzle 后运行本串静态断言；最后全量断言。
- 提交/推送仅在用户授权后进行（本任务尚未授权提交）。

## Skewer 1: 弹窗未开启文案精确化

Files:
- Modify: `app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
- Test: shell grep 断言（无接口变化，纯文案串）

- [ ] Raw: `grep -n "未开启无障碍覆盖，开启后可获得更完整的覆盖范围" app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
      Expected: FAIL（当前为旧串「未开启无障碍覆盖。普通悬浮窗仍可使用；开启后可获得更完整的覆盖范围。」）
- [ ] Sizzle: AlertDialog 未开启分支 text 改为精确串；确认已开启分支文案不动
- [ ] Check temperature: 同上 grep
      Expected: PASS（精确串出现且仅出现在未开启分支）
- [ ] Rest: 无（纯文案替换）
- [ ] Inspect: diff hunk 仅 1 处文案 + 无 import 变化

## Skewer 2: 状态卡无障碍已开启分支的运行提示链

Files:
- Modify: `app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
- Test: shell grep 断言

Interfaces:
- Consumes: `state.error (OverlayError?)`、`state.depthLimited`、`state.appliedDepth`、`state.notificationsAllowed` — 已存在于 MainUiState，无签名变化
- Produces: 副文案字符串（不改变 StatusCard 签名）

- [ ] Raw: `grep -n "WINDOW_REJECTED" app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
      Expected: FAIL（OverlayError import 已被基线删除）
- [ ] Sizzle: 恢复 `import com.h166278.dimveil.overlay.OverlayError`；StatusCard 无障碍已开启分支副文案按 spec 2.3 优先级链实现
- [ ] Check temperature: grep `WINDOW_REJECTED|FOREGROUND_START_FAILED|depthLimited|notificationsAllowed` 四者均 PASS；无障碍未开启分支副文案仍为固定串（grep 确认未被污染）
- [ ] Rest: 若 StatusCard 变长，提取私有 `@Composable` 辅助或私有函数承载副文案计算，保持可读
- [ ] Recheck temperature: 同 Check 断言复跑 PASS
- [ ] Inspect: diff hunk 限于 StatusCard 区域 + import 行

## Skewer 3: 清理与全量静态验收

Files:
- Modify: `app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
- Test: shell 断言脚本（一次性全量）

- [ ] Raw: `grep -c "StatusWarnBg" app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
      Expected: FAIL（当前 1 处=仅色板定义，基线改造后已无引用；目标为 0）
- [ ] Sizzle: 删除未使用的 `StatusWarnBg` 色板定义
- [ ] Check temperature: 全量断言脚本 PASS——
  - `IconButton` / `AccessibilityNew` / `onOpenOverlayPermission` 在 HomeScreen.kt 中 0 引用
  - MainActivity.kt 无 `onOpenOverlayPermission` 参数，且 `openOverlayPermission()` 方法保留并仍被 `onToggle` 回退引用
  - 弹窗两分支文案精确（spec 2.4 两串均在）
  - 状态卡标题：`未开启无障碍覆盖` 与 `无障碍覆盖已开启` 均在
  - OverlayStateLabel 五态文案均在：`当前遮罩：无障碍遮罩`、`当前遮罩：悬浮窗遮罩`、`无障碍遮罩（待开启）`、`悬浮窗遮罩（待开启）`、`需要悬浮窗或无障碍权限`
  - 无 `StatusWarnBg` 残留
- [ ] Rest: 通读最终 diff（git diff 全量），逐 hunk 对照 spec
- [ ] Inspect: 规格-实现逐条核对表（spec 每节 → 代码证据）
- [ ] Commit: 仅当用户授权（本任务未授权 → 跳过，保留工作树）

## Plan cross-examination

### Pass 1 — Coverage deposition（规格→串映射）

| spec 节 | 覆盖串 |
|---|---|
| 2.1 顶部无图标 | 基线已实现；Skewer 3 全量断言锁定 |
| 2.2 开关下方标签 | 基线已实现；Skewer 3 全量断言锁定 |
| 2.3 状态卡（含运行提示链） | Skewer 2 + 3 |
| 2.4 弹窗文案（未开启精确串） | Skewer 1 + 3 |
| 2.5 悬浮窗入口保留 | 基线已实现；Skewer 3 断言锁定 |
| 3 非目标（不动物理层/图标/深度卡） | 全串约束，diff 审阅把关 |

### Pass 2 — Contraband scan

- 无 TBD/TODO/占位符；每步命令与预期证据明确。
- 无越权扩展：不加新图标、不改 MainActivity 权限方法、不重构无关代码。

### Pass 3 — Interface lineup

- HomeScreen/MainActivity 签名已在基线完成变更（onOpenOverlayPermission 移除），本计划不再触碰签名。
- Skewer 2 仅消费 MainUiState 既有字段，无新接口。
- 三串文件交集仅 HomeScreen.kt，串间无并发冲突（Lonely Chef 顺序执行）。

## 已知风险

- 无本地编译：Kotlin 语法正确性只能靠 diff 审阅 + CI 编译。Sizzle 后若发现明显语法风险，记录并交由 CI 验证。
- 基线改动非本人所写：每串改动前重读目标代码段（Artifact rotisserie 规则），不假设行号。
