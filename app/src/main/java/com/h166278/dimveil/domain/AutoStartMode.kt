package com.h166278.dimveil.domain

/** 自动开启遮罩的模式 */
enum class AutoStartMode(val label: String) {
    /** 不自动开启 */
    OFF("不开启"),
    /** 自动使用悬浮窗遮罩 */
    NORMAL("悬浮窗"),
    /** 自动使用无障碍遮罩（未开启无障碍权限时先自动开启权限） */
    ACCESSIBILITY("无障碍")
}
