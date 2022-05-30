package com.example.w_rds_pp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.w_rds_pp.databinding.FragmentSolvedQuoteBinding

class SolvedFragment : Fragment() {
    private lateinit var sq: Solved
    private lateinit var q: Quote

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sq = it.getSerializable("sq") as Solved
            q = it.getSerializable("q") as Quote
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val b = FragmentSolvedQuoteBinding.inflate(inflater, container, false)
        b.time.text = timerFormatter(sq.time)
        childFragmentManager.beginTransaction()
            .add(R.id.quote_fragment_container, QuoteFragment.newInstance(q))
            .commit()
        return b.root
    }

    companion object {
        fun newInstance(solvedQuote: Solved, quote: Quote) =
            SolvedFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("q", quote)
                    putSerializable("sq", solvedQuote)
                }
            }
    }
}