package com.example.w_rds_pp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.w_rds_pp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var pref: SharedPreferences

    private val newGameCreator = registerForActivityResult(NewGameCreatorActivityResultContract()) {
        if(it == NewGameCreatorActivity.NEW_GAME_HAS_BEEN_CREATED)
            onNewGameCreated()
        else onFailedToCreateNewGame(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = getSharedPreferences(PREF_NAME_GAME_STATE, Context.MODE_PRIVATE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.continueGameBtn.setOnClickListener { continueGame() }
        binding.newGameBtn.setOnClickListener { newGame() }

        val historyFragment = AllSolvedQuotesListFragment.newInstance()
        historyFragment.onSolvedSelected = ::onSolvedQuoteSelected
        supportFragmentManager
            .beginTransaction()
            .add(R.id.solved_quotes_fragment, historyFragment)
            .commit()

    }

    override fun onResume() {
        super.onResume()
        refreshContinueBtnActivity()
    }

    private fun continueGame() = if (isThereOngoingGame()) startGame() else newGame()

    private fun newGame() = newGameCreator.launch(null)

    private fun onNewGameCreated() = startGame()

    private fun onFailedToCreateNewGame(code: Int) {
        val msg = "Error while creating new game, error code $code"
        Log.e(TAG, "onFailedToCreateNewGame: $msg")
        Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
    }

    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    private fun isThereOngoingGame(): Boolean =
        readGlobalGameStateFromSharedPreferences(this)?.isCompleted() == false

    private fun refreshContinueBtnActivity() {
        binding.continueGameBtn.isEnabled = isThereOngoingGame()
    }

    private fun onSolvedQuoteSelected(sq: SolvedWithQuote) {
        startActivity(JustShowSolvedActivity.createIntent(this, sq))
    }

    companion object {
        val TAG = MainActivity::class.qualifiedName
    }
}
