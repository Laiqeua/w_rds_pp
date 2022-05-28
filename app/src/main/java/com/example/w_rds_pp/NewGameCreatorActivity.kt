package com.example.w_rds_pp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
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

    private lateinit var db: DataBase
    private lateinit var pref: SharedPreferences

    private var quote: Quote? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(applicationContext, DataBase::class.java, "words_app_db")
            .createFromAsset("populated_db.db")
            .build()

        pref = getSharedPreferences(GAME_SATE_PREF_NAME, Context.MODE_PRIVATE)

        setContentView(R.layout.activity_new_game_creator)

        val selectDifficultyFragment = SelectDifficultyFragment()
        selectDifficultyFragment.onDifficultySelected = ::onDifficultySelected

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container_view, selectDifficultyFragment)
            .commit()

        obtainQuote()
    }

    private fun obtainQuote() = lifecycleScope.launch(Dispatchers.IO) {
        val q = db.quoteDao().findRandom() ?: Quote(-1, "You forget to populate db", ":(")
        quote = q
    }

    private fun onDifficultySelected(d: Double) {
        difficulty = d
        create()
    }

    private fun create() = lifecycleScope.launch(Dispatchers.IO) {
        // todo add max wait time
        while (quote == null) { delay(3) }
        val gs = GameStateHelper.new(quote!!.quote, difficulty)
        gs.saveToPref(CURRENT_GAME_STATE_PREF_KEY, pref)
        setResult(NEW_GAME_HAS_BEEN_CREATED)
        finish()
    }

    companion object {
        const val NEW_GAME_HAS_BEEN_CREATED = 0
    }
}