package com.example.w_rds_pp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.example.w_rds_pp.MGS_AutoSaveToSystemPreferences.Companion.saveToPref

class GameActivity : AppCompatActivity() {
    private lateinit var fragmentContainerView: FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        fragmentContainerView = findViewById(R.id.fragment_container)

        val fragment = GameFragment.newInstance(CURRENT_GAME_STATE_PREF_KEY)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()


    }
}