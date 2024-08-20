package com.example.app_time_counter

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

class MainActivity : AppCompatActivity() {
    lateinit var bindingClass : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingClass = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingClass.root)

        if (hasUsageStatsPermission()) {
            val application = "Phone"
            val usageTimeMillis = getAppUsageTime("com.google.android.dialer")
            val usageTimeSeconds = usageTimeMillis / 1000
            val usageTimeText = DateUtils.formatElapsedTime(usageTimeSeconds)

            bindingClass.usageTimeTextView.text = "Time spent in $application : $usageTimeText"
        } else {
            requestUsageStatsPermission()
        }
    }

    private fun getAppUsageTime(application: String): Long {
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
            if (usageStats.packageName == application) {
                totalTime += usageStats.totalTimeInForeground
            }
        }
        return totalTime
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
        Toast.makeText(this, "Пожалуйста, предоставьте разрешение на использование статистики", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }
}