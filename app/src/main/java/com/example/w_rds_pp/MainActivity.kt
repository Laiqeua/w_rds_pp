package com.example.w_rds_pp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var newGameBtn: Button
    private lateinit var continueGameBtn: Button

    private lateinit var pref: SharedPreferences

    private val newGameCreator = registerForActivityResult(NewGameCreatorActivityResultContract()) {
        if(it == NewGameCreatorActivity.NEW_GAME_HAS_BEEN_CREATED)
            onNewGameCreated()
        else onFailedToCreateNewGame(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = getSharedPreferences(GAME_SATE_PREF_NAME, Context.MODE_PRIVATE)

        setContentView(R.layout.activity_main)

        newGameBtn = findViewById(R.id.new_game_btn)
        continueGameBtn = findViewById(R.id.continue_game_btn)

        continueGameBtn.setOnClickListener { continueGame() }
        newGameBtn.setOnClickListener { newGame() }

        val historyFragment = AllSolvedQuotesListFragment.newInstance()
        historyFragment.onSolvedSelected = ::onSolvedQuoteSelected
        supportFragmentManager
            .beginTransaction()
            .add(R.id.solved_quotes_fragment, historyFragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        refreshContinueBtnVisibility()
    }

    private fun continueGame() = if (isThereOngoingGame()) startGame() else newGame()

    private fun newGame() = newGameCreator.launch(null)

    private fun onNewGameCreated() = startGame()
    private fun onFailedToCreateNewGame(code: Int) {
        Toast.makeText(applicationContext, "Error while creating new game", Toast.LENGTH_LONG).show()
    }

    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    private fun isThereOngoingGame(): Boolean {
        if(!pref.contains(CURRENT_GAME_STATE_PREF_KEY)) return false
        return !GameState.deserializeGameState(pref.getString(CURRENT_GAME_STATE_PREF_KEY, "")!!).isCompleted()
    }

    private fun refreshContinueBtnVisibility() {
        continueGameBtn.isEnabled = isThereOngoingGame()
    }

    private fun onSolvedQuoteSelected(sq: SolvedWithQuote) {
        startActivity(JustShowSolvedActivity.createIntent(this, sq))
    }

    companion object {
        val TAG = MainActivity::class.qualifiedName
    }
}
