package com.example.w_rds_pp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.w_rds_pp.MGS_AutoSaveToSystemPreferences.Companion.saveToPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewGameCreatorActivityResultContract : ActivityResultContract<Unit, Int>() {
    override fun createIntent(context: Context, input: Unit?): Intent = Intent(context, NewGameCreatorActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): Int = resultCode

}

class NewGameCreatorActivity : AppCompatActivity() {
    private var difficulty: Double = 0.0
    // category == null means any category
    private var category: String? = null

    private lateinit var db: AppsDatabase
    private lateinit var pref: SharedPreferences

    private var quote: Quote? = null

    private lateinit var catFragment: SelectCategoryFragment
    private lateinit var diffFragment: SelectDifficultyFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(applicationContext, AppsDatabase::class.java, "words_app3")
            .createFromAsset("populated_db.db")
            .build()
        pref = getSharedPreferences(GAME_SATE_PREF_NAME, Context.MODE_PRIVATE)
        setContentView(R.layout.activity_new_game_creator)
        runCategorySelector()
    }

    private fun obtainQuote() = lifecycleScope.launch(Dispatchers.IO) {
        quote = (if(category == null) db.quoteDao().findRandom() else db.quoteDao().findRandomWhereCategory(category!!))
            ?: Quote(-1, "You forget to populate db", ":(")
    }

    private fun runCategorySelector() = lifecycleScope.launch(Dispatchers.IO) {
        val categories = db.quoteDao().findCategories()
        catFragment = SelectCategoryFragment.newInstance(categories)
        catFragment.onCategorySelected = ::onCategorySelected
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container_view, catFragment)
            .commit()
    }

    private fun runDifficultySelector() {
        diffFragment = SelectDifficultyFragment()
        diffFragment.onDifficultySelected = ::onDifficultySelected
        supportFragmentManager
            .beginTransaction()
            .remove(catFragment)
            .add(R.id.fragment_container_view, diffFragment)
            .commit()
    }

    private fun onCategorySelected(c: String?){
        category = c
        obtainQuote()
        runDifficultySelector()
    }

    private fun onDifficultySelected(d: Double) {
        difficulty = d
        create()
    }

    private fun create() = lifecycleScope.launch(Dispatchers.IO) {
        // todo add max wait time
        while (quote == null) { delay(3) }
        val gs = GameState.new(quote!!.quote, difficulty)
        gs.saveToPref(CURRENT_GAME_STATE_PREF_KEY, pref)
        setResult(NEW_GAME_HAS_BEEN_CREATED)
        finish()
    }

    companion object {
        const val NEW_GAME_HAS_BEEN_CREATED = 0
    }
}