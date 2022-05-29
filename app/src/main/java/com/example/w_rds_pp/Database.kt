package com.example.w_rds_pp

import android.content.Context
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

    @Query("select * from quote where category = (:category) and id not in (:except) order by random() limit 1")
    fun findRandomWhereCategory(category: String, except: List<Long> = emptyList()): Quote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(quote: Quote): Long

    @Query("""
        select category
        from quote
        group by category
    """)
    fun findCategories(): List<String>
}

@Database(entities = [Quote::class], version = 1, exportSchema = true)
abstract class AppsDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    companion object {
        private var OBJ: AppsDatabase? = null
        fun instance(context: Context): AppsDatabase {
            if(OBJ != null) return OBJ!!
            synchronized(this) {
                if(OBJ != null) return OBJ!!
                OBJ = Room.databaseBuilder(context, AppsDatabase::class.java, "AppsDatabase2")
                          .createFromAsset("quotes_db_1.db")
                          .build()
                return OBJ!!
            }
        }
    }
}
