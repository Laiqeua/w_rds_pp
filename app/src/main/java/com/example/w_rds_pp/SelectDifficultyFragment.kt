package com.example.w_rds_pp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class SelectDifficultyFragment : Fragment() {
    lateinit var easyBtn: Button
    lateinit var normalBtn: Button
    lateinit var hardBtn: Button
    lateinit var ultraBtn: Button

    var onDifficultySelected: (Double) -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_select_difficulty, container, false)
        easyBtn = v.findViewById(R.id.diff_easy)
        normalBtn = v.findViewById(R.id.diff_normal)
        hardBtn = v.findViewById(R.id.diff_hard)
        ultraBtn = v.findViewById(R.id.diff_ultra)
        easyBtn.setOnClickListener { onDifficultySelected(0.51) }
        normalBtn.setOnClickListener { onDifficultySelected(0.79) }
        hardBtn.setOnClickListener { onDifficultySelected(0.91) }
        ultraBtn.setOnClickListener { onDifficultySelected(1.0) }
        return v
    }
}