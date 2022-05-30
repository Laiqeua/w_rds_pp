package com.example.w_rds_pp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        val db = AppsDatabase.instance(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            val id = db.solvedDao().insert(Solved(null, gs.quote.id ?: -1, gs.howLongIsBeingSolvedSec))
            val sq = db.solvedWithQuoteDao().selectSolvedWithQuoteBySolvedId(id)
            sq ?: run {
                Log.e(TAG, "onPuzzleCompeted: probably db.solvedDao().insert() inserts in bg")
                finishActivity(-1)
                return@launch
            }
            lifecycleScope.launch(Dispatchers.Main) {
                val congratulationFragment = CongratulationFragment.newInstance(sq)
                supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.enter, R.anim.exit)
                    .replace(R.id.fragment_container, congratulationFragment)
                    .commit()
            }
        }
    }

    companion object {
        val TAG = GameActivity::class.qualifiedName
    }
}