package com.heodongun.ugoal.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.heodongun.ugoal.utils.NotificationHelper
import java.time.LocalDateTime
import java.time.ZoneId

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getStringExtra("todoId") ?: return
        val title = intent.getStringExtra("title") ?: "할 일 알림"
        val description = intent.getStringExtra("description") ?: ""

        NotificationHelper.showTodoReminder(context, todoId, title, description)
    }

    companion object {
        fun scheduleTodoReminder(
            context: Context,
            todoId: String,
            title: String,
            description: String,
            reminderTimeMillis: Long
        ) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("todoId", todoId)
                putExtra("title", title)
                putExtra("description", description)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                todoId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            }
        }

        fun cancelTodoReminder(context: Context, todoId: String) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                todoId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
}
