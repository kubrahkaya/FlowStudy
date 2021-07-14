package com.example.flowstudy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Plant::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
}