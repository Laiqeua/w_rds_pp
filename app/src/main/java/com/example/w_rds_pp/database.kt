package com.example.w_rds_pp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class QuoteDAO (
    @PrimaryKey val uid: Int,
    val quote: String,
    val category: String,
)