package com.example.app_time_counter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "user")
data class User (
    @PrimaryKey
    @ColumnInfo(name = "android_id")
    var androidId: String,

    @ColumnInfo(name = "long_value")
    var timeSpentInApp: String
)