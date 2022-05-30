package com.example.w_rds_pp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity
data class Quote (
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val quote: String,
    val category: String,
)

@Entity
data class Solved(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val quoteId: Long,
    val time: Int,
)

data class SolvedWithQuote(
    @Embedded(prefix = "s_")
    val solved: Solved,

    @Embedded(prefix = "q_")
    val quote: Quote,
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
        order by category
    """)
    fun findCategories(): List<String>
}

@Dao
interface SolvedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sq: Solved): Long
}

@Dao
interface SolvedWithQuoteDao {
    @Query("""
        select q.id as q_id,
               q.quote as q_quote,
               q.category as q_category,
               s.id as s_id,
               q.id as s_quoteId,
               s.time as s_time
        from Solved as s
        join Quote as q on q.id = s.quoteId
        order by s.id desc
        """)
    fun selectSolvedWithQuote(): LiveData<List<SolvedWithQuote>>
        @Query("""
        select q.id as q_id,
               q.quote as q_quote,
               q.category as q_category,
               s.id as s_id,
               q.id as s_quoteId,
               s.time as s_time
        from Solved as s
        join Quote as q on q.id = s.quoteId
        where s.id = (:id)
        order by s.id desc
        """)
    fun selectSolvedWithQuoteBySolvedId(id: Long): SolvedWithQuote?


}

@Database(
    entities = [Quote::class, Solved::class],
    version = 1,
    exportSchema = true,
)
abstract class AppsDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun solvedDao(): SolvedDao
    abstract fun solvedWithQuoteDao(): SolvedWithQuoteDao
    companion object {
        private var OBJ: AppsDatabase? = null
        fun instance(context: Context): AppsDatabase {
            if(OBJ != null) return OBJ!!
            synchronized(this) {
                if(OBJ != null) return OBJ!!
                OBJ = Room.databaseBuilder(context, AppsDatabase::class.java, "WordsAppDatabase4")
                          .createFromAsset("quotes.db")
                          .build()
                return OBJ!!
            }
        }
    }
}
