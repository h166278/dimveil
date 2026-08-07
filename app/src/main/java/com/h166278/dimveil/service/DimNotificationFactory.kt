package com.h166278.dimveil.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.h166278.dimveil.MainActivity
import com.h166278.dimveil.R

object DimNotificationFactory {
    const val CHANNEL_ID = "dimveil_overlay"
    const val NOTIFICATION_ID = 1001
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    fun notification(context: Context, depth: Int, mode: String): Notification {
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val close = PendingIntent.getService(context, 1, OverlayService.stopIntent(context), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_dimveil).setContentTitle(context.getString(R.string.notification_title)).setContentText("遮罩深度 ${depth}% · $mode").setContentIntent(open).setOngoing(true).addAction(0, context.getString(R.string.close_overlay), close).build()
    }
}
