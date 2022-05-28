package com.example.w_rds_pp

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.example.w_rds_pp.MGS_AutoSaveToSystemPreferences.Companion.saveToPref

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
        val pref = getSharedPreferences(GAME_SATE_PREF_NAME, Activity.MODE_PRIVATE)
        val gs = GameStateHelper.deserializeGameState(pref.getString(CURRENT_GAME_STATE_PREF_KEY, "")!!)
        val congratulationFragment = CongratulationFragment.newInstance(gs.originalText)
        supportFragmentManager
            .beginTransaction()
            .remove(gameFragment)
            .add(R.id.fragment_container, congratulationFragment)
            .commit()
    }

}