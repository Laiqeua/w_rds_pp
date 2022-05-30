package com.example.w_rds_pp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.w_rds_pp.databinding.FragmentCongratulationBinding

class CongratulationFragment : Fragment() {
    private lateinit var sq: SolvedWithQuote

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sq = GsonInstance.fromJson(it.getString("sq"), SolvedWithQuote::class.java)
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
                    putString("sq", GsonInstance.toJson(sq))
                }
            }
    }
}