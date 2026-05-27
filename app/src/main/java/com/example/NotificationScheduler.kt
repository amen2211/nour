package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.util.Calendar

object NotificationScheduler {
    private const val TAG = "NotificationScheduler"
    
    private val PRAYERS_AR = mapOf(
        "Fajr" to "الفجر",
        "Dhuhr" to "الظهر",
        "Asr" to "العصر",
        "Maghrib" to "المغرب",
        "Isha" to "العشاء"
    )

    fun schedulePrayerTimes(context: Context, jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            for ((key, arName) in PRAYERS_AR) {
                if (jsonObject.has(key)) {
                    val rawTime = jsonObject.getString(key)
                    val parts = rawTime.split(":")
                    if (parts.size == 2) {
                        val hour = parts[0].toInt()
                        val minute = parts[1].toInt()

                        scheduleAlarmForPrayer(context, alarmManager, key, arName, hour, minute)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling prayer times from JSON: ${e.message}", e)
        }
    }

    private fun scheduleAlarmForPrayer(
        context: Context,
        alarmManager: AlarmManager,
        key: String,
        arName: String,
        hour: Int,
        minute: Int
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("PRAYER_NAME", arName)
            putExtra("PRAYER_TIME", String.format("%02d:%02d", hour, minute))
        }

        val requestCode = key.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d(TAG, "Scheduling alarm for $arName ($key) at ${calendar.time} (millis=${calendar.timeInMillis})")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
