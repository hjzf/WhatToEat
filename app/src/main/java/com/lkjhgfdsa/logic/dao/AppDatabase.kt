package com.lkjhgfdsa.logic.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lkjhgfdsa.ThisApplication
import com.lkjhgfdsa.logic.model.Option

@Database(entities = [Option::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun optionDao(): OptionDao

    companion object {
        private const val DB_NAME = "what_to_eat.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context = ThisApplication.context): AppDatabase {
            if (instance == null) {
                synchronized(AppDatabase::class.java) {
                    if (instance == null) {
                        val javaClass = AppDatabase::class.java
                        val databaseBuilder = Room.databaseBuilder(context, javaClass, DB_NAME)
                        instance = databaseBuilder.build()
                    }
                }
            }
            return instance!!
        }
    }
}