package com.h166278.dimveil.overlay

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 通过 Shizuku（shell 权限）直接开启/关闭暗幕的无障碍服务授权，
 * 行为等价于系统「无障碍快捷方式」，无需跳转系统设置页。
 *
 * 原理：修改 Settings.Secure 的 enabled_accessibility_services 列表，
 * 系统 AccessibilityManagerService 监听变化后动态绑定/解绑服务。
 * 前提：手机已运行 Shizuku 且用户已在 Shizuku 授权界面授权本应用。
 */
object ShizukuAccessibility {
    const val REQUEST_CODE = 10086
    private const val SERVICE_COMPONENT =
        "com.h166278.dimveil/com.h166278.dimveil.service.DimAccessibilityService"

    /** Shizuku 服务端是否在运行 */
    fun isAvailable(): Boolean = Shizuku.pingBinder()

    /** 本应用是否已被 Shizuku 授权 */
    fun isGranted(): Boolean =
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    /** 弹出 Shizuku 授权申请（首次需要，用户同意后写入授权记录） */
    fun requestPermission() {
        Shizuku.requestPermission(REQUEST_CODE)
    }

    /**
     * 切换无障碍授权状态，返回切换后的状态（true = 已开启）。
     * 仅在 [isAvailable] && [isGranted] 成立时调用。
     */
    suspend fun toggle(): Boolean = withContext(Dispatchers.IO) {
        val list = readEnabledServices()
        val enabled = list.contains(SERVICE_COMPONENT)
        if (enabled) disable(list) else enable(list)
        !enabled
    }

    /**
     * 开启无障碍授权（仅 [isAvailable] && [isGranted] 成立时调用）。
     * 返回是否成功。
     */
    suspend fun turnOn(): Boolean = withContext(Dispatchers.IO) {
        try {
            val list = readEnabledServices()
            enable(list)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun enable(list: List<String>) {
        val next = if (list.isEmpty()) SERVICE_COMPONENT else (list + SERVICE_COMPONENT).joinToString(":")
        exec("settings", "put", "secure", "enabled_accessibility_services", next)
        exec("settings", "put", "secure", "accessibility_enabled", "1")
    }

    private fun disable(list: List<String>) {
        val remaining = list.filter { it != SERVICE_COMPONENT && it.isNotEmpty() }
        if (remaining.isEmpty()) {
            exec("settings", "put", "secure", "enabled_accessibility_services", "")
            exec("settings", "put", "secure", "accessibility_enabled", "0")
        } else {
            exec("settings", "put", "secure", "enabled_accessibility_services", remaining.joinToString(":"))
        }
    }

    private fun readEnabledServices(): List<String> {
        val raw = exec("settings", "get", "secure", "enabled_accessibility_services")
        if (raw.isEmpty() || raw == "null") return emptyList()
        return raw.split(":").filter { it.isNotEmpty() }
    }

    /** 以 shell 身份执行命令，返回 stdout（去空白）；非零退出码抛异常 */
    private fun exec(vararg args: String): String {
        // vararg 展开后是 Array<out String>，反射签名需要 Array<String>
        val process = newProcess(arrayOf(*args))
        val output = process.inputStream.bufferedReader().readText().trim()
        val exit = process.waitFor()
        if (exit != 0) throw IOException("shizuku exec failed (exit=$exit): ${args.joinToString(" ")}")
        return output
    }

    /**
     * Shizuku 13.x 的 newProcess 为 private static，官方未提供公开替代入口，
     * 通过反射调用（社区标准做法，返回值 ShizukuRemoteProcess 继承 java.lang.Process）。
     */
    private fun newProcess(cmd: Array<String>): Process {
        val method = Shizuku::class.java
            .getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
        method.isAccessible = true
        return method.invoke(null, cmd, null, null) as Process
    }
}
