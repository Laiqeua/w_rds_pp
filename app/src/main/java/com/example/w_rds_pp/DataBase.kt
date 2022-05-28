package com.example.w_rds_pp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Quote::class], version = 1, exportSchema = true)
abstract class DataBase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
}