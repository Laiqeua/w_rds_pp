package com.example.w_rds_pp

import androidx.room.*

@Entity
data class Quote (
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val quote: String,
    val category: String,
)

@Dao
interface QuoteDao {
    // todo will db optimalize this or it will select every row, sort and choose first ?
    @Query("select * from quote where id not in (:except) order by random() limit 1")
    fun findRandom(except: List<Long> = emptyList()): Quote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(quote: Quote): Long
}

