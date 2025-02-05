package com.example.app_time_counter

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 1)
abstract class MainDb : RoomDatabase() {
    abstract fun getDao() : Dao
    companion object{
        fun getDb(context: Context) : MainDb{ //создаем функцию, которая будет выдавать бд
            return Room.databaseBuilder(
                context.applicationContext,
                MainDb::class.java,
                "UserDb"
            ).build()
        }
    }
}