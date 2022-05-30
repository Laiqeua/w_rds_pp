package com.example.w_rds_pp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonSyntaxException

class JustShowSolvedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solved_quote_view)
        try {
            val sq = intent.getSerializableExtra("sq") as SolvedWithQuote
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, SolvedFragment.newInstance(sq.solved, sq.quote))
                .commit()
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "sq must be to json serialized SolvedWithQuote (You can use static createIntent())", e)
            return
        }
    }

    companion object {
        val TAG: String = JustShowSolvedActivity::class.java.name
        fun createIntent(context: Context, sq: SolvedWithQuote): Intent =
            Intent(context, JustShowSolvedActivity::class.java).apply {
                putExtra("sq", sq)
            }
    }
}