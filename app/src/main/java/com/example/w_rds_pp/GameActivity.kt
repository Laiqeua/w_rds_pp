package com.example.w_rds_pp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.example.w_rds_pp.GameStateHelper.serialize

class GameActivity : AppCompatActivity() {
    private lateinit var fragmentContainerView: FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        fragmentContainerView = findViewById(R.id.fragment_container)

        val prefKey = "game 444"

        val gs = GameStateHelper.new(
            "Why, dear boy, we don't send wizards to Azkaban just for blowing up their aunts."
        )
        val pref = getSharedPreferences(GAME_SATE_PREF_NAME, MODE_PRIVATE)
        pref.edit().putString(prefKey, gs.serialize()).apply()

        val fragment = GameFragment.newInstance(prefKey)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()


    }
}