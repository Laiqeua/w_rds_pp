package com.example.w_rds_pp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity
data class Quote (
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val quote: String,
    val category: String,
) : java.io.Serializable

@Entity
data class Solved(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val quoteId: Long,
    val time: Int,
) : java.io.Serializable

data class SolvedWithQuote(
    @Embedded(prefix = "s_")
    val solved: Solved,

    @Embedded(prefix = "q_")
    val quote: Quote,
) : java.io.Serializable

data class CategoryWithNumberOfUnsolvedQuotes(
    val category: String,
    val n: Int
)

@Dao
interface AllInOneDao {
    // todo will db optimalize this or it will select every row, sort and choose first ?
    @Query("select * from quote order by random() limit 1")
    fun findRandomQuote(): Quote?

    @Query("select * from quote where category = (:category) order by random() limit 1")
    fun findRandomQuoteWhereCategory(category: String): Quote?

    @Query("select * from quote where id not in (select quoteID from Solved) order by random() limit 1")
    fun findRandomNotSolvedQuote(): Quote?

    @Query("""select * from quote
              where id not in (select quoteID from Solved)
                    and category = (:category)
              order by random()
              limit 1""")
    fun findRandomNotSolvedQuoteWhereCategory(category: String): Quote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuote(quote: Quote): Long

    @Query("select category from quote group by category order by category")
    fun findCategories(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSolved(sq: Solved): Long

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
    abstract fun dao(): AllInOneDao
    companion object {
        private var OBJ: AppsDatabase? = null
        fun instance(context: Context): AppsDatabase {
            if(OBJ != null) return OBJ!!
            synchronized(this) {
                if(OBJ != null) return OBJ!!
                OBJ = Room.databaseBuilder(context, AppsDatabase::class.java, "WordsAppDatabase5")
                          .createFromAsset("quotes.db")
                          .build()
                return OBJ!!
            }
        }
    }
}
