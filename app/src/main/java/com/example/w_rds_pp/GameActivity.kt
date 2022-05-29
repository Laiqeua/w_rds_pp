package com.example.w_rds_pp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    private lateinit var gameFragment: GameFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameFragment = GameFragment.newInstance(CURRENT_GAME_STATE_PREF_KEY)
        gameFragment.onPuzzleCompleted = ::onPuzzleCompeted

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, gameFragment)
            .commit()
    }

    private fun onPuzzleCompeted() {
        val gs = readGlobalGameStateFromSharedPreferences(this) ?: run {
            Log.e(TAG, "onPuzzleCompeted: gs is null")
            return
        }
        val congratulationFragment = CongratulationFragment.newInstance(gs.originalText, timerFormatter(gs.howLongIsBeingSolvedSec))
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.enter, R.anim.exit)
            .replace(R.id.fragment_container, congratulationFragment)
            .commit()
    }

    companion object {
        val TAG = GameActivity::class.qualifiedName
    }
}