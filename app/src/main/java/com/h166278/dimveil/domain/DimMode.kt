package com.h166278.dimveil.domain

enum class DimMode(val label: String, val defaultDepth: Int) {
    NIGHT("夜间", 62), READING("阅读", 42), GAME("游戏", 28), CUSTOM("自定义", 50);
    fun depth(customDepth: Int): Int = if (this == CUSTOM) customDepth.coerceIn(0, 90) else defaultDepth
    companion object { fun clamp(depth: Int) = depth.coerceIn(0, 90) }
}
