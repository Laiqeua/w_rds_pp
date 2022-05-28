package com.example.w_rds_pp

import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.w_rds_pp.MGS_AutoSaveToSystemPreferences.Companion.saveToPrefNow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var newGameBtn: Button
    private lateinit var continueGameBtn: Button

    private lateinit var db: DataBase

    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(applicationContext, DataBase::class.java, "words_app_db")
            .createFromAsset("populated_db.db")
            .build()

        pref = getSharedPreferences(GAME_SATE_PREF_NAME, Context.MODE_PRIVATE)

        setContentView(R.layout.activity_main)

        newGameBtn = findViewById(R.id.new_game_btn)
        continueGameBtn = findViewById(R.id.continue_game_btn)

        continueGameBtn.setOnClickListener { continueGame() }
        newGameBtn.setOnClickListener { newGame() }
    }

    override fun onResume() {
        super.onResume()
        refreshContinueBtnVisibility()
    }

    private fun continueGame() = if (isThereOngoingGame()) startGame() else newGame()

    private fun newGame() = lifecycleScope.launch(Dispatchers.IO) {
        val newQuote: Quote = db.quoteDao().findRandom() ?: run {
            Log.d(TAG, "newGame: populate db !")
            return@launch
        }
        val GS = GameStateHelper.new(newQuote.quote)
        GS.saveToPrefNow(CURRENT_GAME_STATE_PREF_KEY, pref)
        startGame()
    }

    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    private fun isThereOngoingGame() = pref.contains(CURRENT_GAME_STATE_PREF_KEY)

    private fun refreshContinueBtnVisibility() {
        continueGameBtn.visibility = (if(!isThereOngoingGame()) View.GONE else View.VISIBLE)
    }

    companion object {
        val TAG = MainActivity::class.qualifiedName
    }
}
