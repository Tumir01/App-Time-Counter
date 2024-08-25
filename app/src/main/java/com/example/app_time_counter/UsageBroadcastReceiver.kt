package com.example.app_time_counter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.usage.UsageStatsManager
import android.provider.Settings
import android.text.format.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UsageBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 1000 * 60 * 60 * 24

            val usageStatsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            var totalTime: Long = 0

            for (usageStats in usageStatsList) {
                if (usageStats.packageName == "com.instagram.android") {
                    totalTime += usageStats.totalTimeInForeground
                }
            }

            val usageTimeMillis = totalTime
            val usageTimeSeconds = usageTimeMillis / 1000
            val usageTimeText = DateUtils.formatElapsedTime(usageTimeSeconds)

            val db = MainDb.getDb(context)
            val androidId = getAndroidId(context)

            val existingUser = db.getDao().getUserByAndroidId(androidId)
            if (existingUser != null) {
                existingUser.timeSpentInApp = usageTimeText
                db.getDao().updateUser(existingUser)
            }
        }
    }

    private fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
