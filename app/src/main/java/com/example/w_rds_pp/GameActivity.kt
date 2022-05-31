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

        gameFragment = GameFragment.newInstance(CURRENT_GAME_PREF_INFO)
        gameFragment.onPuzzleCompleted = ::onPuzzleCompeted

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, gameFragment)
            .commit()
    }

    private fun onPuzzleCompeted() {
        val gs = readGlobalGameStateFromSharedPreferences(this) ?: run {
            Log.e(TAG, "onPuzzleCompeted: should not happen gs is null")
            return
        }

        val db = WordsAppDatabase.instance(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            val id = db.dao().insertSolved(Solved(null, gs.quote.id ?: -1, gs.howLongIsBeingSolvedSec))
            val sq = db.dao().selectSolvedWithQuoteBySolvedId(id)
            sq ?: run {  // todo checking such things seems like being a little too caring
                Log.e(TAG, "onPuzzleCompeted: it should not be null, it looks like db queries are no done in bg")
                finishActivity(-666)
                return@launch
            }
            lifecycleScope.launch(Dispatchers.Main) {
                val congratsFragment = CongratulationFragment.newInstance(sq)
                supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.enter, R.anim.exit)
                    .replace(R.id.fragment_container, congratsFragment)
                    .commit()
            }
        }
    }

    companion object {
        val TAG = GameActivity::class.qualifiedName
    }
}