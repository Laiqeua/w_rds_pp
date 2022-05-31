package com.example.w_rds_pp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.w_rds_pp.databinding.FragmentSelectDifficultyBinding

class SelectDifficultyFragment private constructor(): Fragment() {
    var onDifficultySelected: (Difficulty) -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val b = FragmentSelectDifficultyBinding.inflate(inflater, container, false)
        b.diffEasy.setOnClickListener { onDifficultySelected(Difficulty.EASY) }
        b.diffNormal.setOnClickListener { onDifficultySelected(Difficulty.NORMAL) }
        b.diffHard.setOnClickListener { onDifficultySelected(Difficulty.HARD) }
        b.diffUltra.setOnClickListener { onDifficultySelected(Difficulty.ULTRA) }
        return b.root
    }

    companion object {
        fun newInstance() = SelectDifficultyFragment()
    }
}