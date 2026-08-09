package com.h166278.dimveil.domain

data class DimSettings(
    val mode: DimMode = DimMode.NIGHT,
    val depth: Int = DimMode.NIGHT.defaultDepth,
    val customDepth: Int = DimMode.CUSTOM.defaultDepth,
    /** 自动开启遮罩的模式：不开启 / 悬浮窗遮罩 / 无障碍遮罩 */
    val autoStartMode: AutoStartMode = AutoStartMode.OFF
)
