package com.example.w_rds_pp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.lifecycleScope
import com.example.w_rds_pp.MGS_AutoSaveToSystemPreferences.Companion.saveToPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewGameCreatorActivityResultContract : ActivityResultContract<Unit, Int>() {
    override fun createIntent(context: Context, input: Unit?): Intent =
        Intent(context, NewGameCreatorActivity::class.java)
    override fun parseResult(resultCode: Int, intent: Intent?): Int = resultCode
}

class NewGameCreatorActivity : AppCompatActivity() {
    private var difficulty: Difficulty = Difficulty.DEFAULT
    // category == null means any category
    private var category: String? = null

    private lateinit var db: AppsDatabase
    private lateinit var pref: SharedPreferences

    private var quote: Quote? = null

    private lateinit var catFragment: SelectCategoryFragment
    private lateinit var diffFragment: SelectDifficultyFragment

    override fun onBackPressed() {
        finishActivity(BACK_BTN)
        val intent = Intent(this, MainActivity::class.java)
        // FLAG_ACTIVITY_CLEAR_TOP should make it launch running activity
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppsDatabase.instance(applicationContext)
        pref = getSharedPreferences(GAME_SATE_PREF_NAME, Context.MODE_PRIVATE)
        setContentView(R.layout.activity_new_game_creator)
        runCategorySelector()
    }

    private fun obtainQuote() = lifecycleScope.launch(Dispatchers.IO) {
        val firstAttempt = if (category == null) db.dao().findRandomNotSolvedQuote()
                           else db.dao().findRandomNotSolvedQuoteWhereCategory(category!!)
        quote = if (firstAttempt == null) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            applicationContext,
                            "You solved all from selected category, " +
                                "Don't worry, You can always solve some of them again :)",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    (if (category == null) db.dao().findRandomQuote()
                    else db.dao().findRandomQuoteWhereCategory(category!!)) ?: Quote(-1, "You forgot to populate db", ":(")
                } else {
                    firstAttempt
                }
    }
    private fun runCategorySelector() = lifecycleScope.launch(Dispatchers.IO) {
        val categories = db.dao().findCategories()
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
            .setCustomAnimations(R.anim.enter, R.anim.exit)
            .replace(R.id.fragment_container_view, diffFragment)
            .commit()
    }

    private fun onCategorySelected(c: String?){
        category = c
        obtainQuote()
        runDifficultySelector()
    }

    private fun onDifficultySelected(d: Difficulty) {
        difficulty = d
        create()
    }

    private fun create() = lifecycleScope.launch(Dispatchers.IO) {
        // todo add max wait time
        while (quote == null) { delay(3) }
        val gs = GameState.new(quote!!, difficulty)
        gs.saveToPref(CURRENT_GAME_STATE_PREF_KEY, pref)
        setResult(NEW_GAME_HAS_BEEN_CREATED)
        finish()
    }

    companion object {
        const val NEW_GAME_HAS_BEEN_CREATED = 123
        const val BACK_BTN = 321
    }
}