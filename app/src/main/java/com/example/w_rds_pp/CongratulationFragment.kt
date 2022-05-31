package com.example.w_rds_pp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.w_rds_pp.databinding.FragmentCongratulationBinding

class CongratulationFragment private constructor(): Fragment() {
    private lateinit var sq: SolvedWithQuote

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sq = it.getSerializable("sq") as SolvedWithQuote
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val b = FragmentCongratulationBinding.inflate(inflater, container, false)
        childFragmentManager.beginTransaction()
            .add(R.id.fragment_container, SolvedFragment.newInstance(sq.solved, sq.quote))
            .commit()
        return b.root
    }

    companion object {
        fun newInstance(sq: SolvedWithQuote) =
            CongratulationFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("sq", sq)
                }
            }
    }
}