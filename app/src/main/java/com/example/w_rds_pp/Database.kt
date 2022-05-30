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
data class SolvedQuote(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val quoteId: Long,
    val time: Int,
)

@DatabaseView("""
    select q.id as quoteId,
           q.quote,
           q.category,
           sq.id as solvedQuoteId,
           sq.time
    from SolvedQuote as sq
    join Quote as q on q.id = sq.quoteId
    order by sq.id desc
""")
data class SolvedQuoteWithQuote(
    val quoteId: Long,
    val quote: String,
    val category: String,
    val solvedQuoteId: Long,
    val time: Int,
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
interface SolvedQuoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sq: SolvedQuote): Long

    @Query("select * from SolvedQuoteWithQuote")
    fun selectSolvedQuotesWithQuotes(): LiveData<List<SolvedQuoteWithQuote>>
}

@Database(
    entities = [Quote::class, SolvedQuote::class],
    views =[SolvedQuoteWithQuote::class],
    version = 1,
    exportSchema = true,
)
abstract class AppsDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun solvedQuoteDao(): SolvedQuoteDao
    companion object {
        private var OBJ: AppsDatabase? = null
        fun instance(context: Context): AppsDatabase {
            if(OBJ != null) return OBJ!!
            synchronized(this) {
                if(OBJ != null) return OBJ!!
                OBJ = Room.databaseBuilder(context, AppsDatabase::class.java, "WordsAppDatabase3")
                          .createFromAsset("quotes.db")
                          .build()
                return OBJ!!
            }
        }
    }
}
