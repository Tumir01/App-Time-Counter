package com.example.app_time_counter

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.format.DateUtils
import android.widget.Toast
import com.example.app_time_counter.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (hasUsageStatsPermission()) {
        appTimeCounter("Instagram", "com.instagram.android")
        setupAlarm()
        } else {
            requestUsageStatsPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        appTimeCounter("Instagram", "com.instagram.android")
    }

    private fun appTimeCounter(applicationName: String, packageName: String) {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 60 * 24

        val usageStatsList: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        var totalTime: Long = 0

        for (usageStats in usageStatsList) {
            if (usageStats.packageName == packageName) {
                totalTime += usageStats.totalTimeInForeground
            }
        }

        val usageTimeMillis = totalTime
        val usageTimeSeconds = usageTimeMillis / 1000
        val usageTimeText = DateUtils.formatElapsedTime(usageTimeSeconds)

        binding.usageTimeTextView.text = "Time spent in $applicationName: $usageTimeText"

        val db = MainDb.getDb(this)
        val androidId = getAndroidId(this)

        CoroutineScope(Dispatchers.IO).launch {
            val existingUser = db.getDao().getUserByAndroidId(androidId)

            if (existingUser != null) {
                existingUser.timeSpentInApp = usageTimeText
                db.getDao().updateUser(existingUser)
            } else {
                val newUser = User(
                    androidId = androidId,
                    timeSpentInApp = usageTimeText
                )
                db.getDao().insertUser(newUser)
            }
        }
    }

    fun clearDatabase(context: Context) {
        context.deleteDatabase("UserDb")
    }

    private fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun hasUsageStatsPermission(): Boolean {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val appStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            System.currentTimeMillis() - 1000 * 60 * 60 * 24,
            System.currentTimeMillis()
        )
        return appStats.isNotEmpty()
    }

    private fun requestUsageStatsPermission() {
        Toast.makeText(this, "Please grant usage stats permission", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private fun setupAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, UsageBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            1000 * 60,
            pendingIntent
        )
    }
}
