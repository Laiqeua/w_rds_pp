package com.example.w_rds_pp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.example.w_rds_pp.databinding.FragmentAllSolvedQuotesListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// todo create row fragment

// todo quotes should be provided to fragment

class AllSolvedQuotesListFragment : Fragment() {
    private lateinit var b: FragmentAllSolvedQuotesListBinding

    private lateinit var li: LayoutInflater

    var onSolvedSelected: (SolvedWithQuote) -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentAllSolvedQuotesListBinding.inflate(inflater, container, false)
        li = inflater

        val db = WordsAppDatabase.instance(requireContext())
        lifecycleScope.launch(Dispatchers.IO) {
            val liveData = db.dao().selectSolvedWithQuote()
            lifecycleScope.launch(Dispatchers.Main) {
                liveData.observe(viewLifecycleOwner) {
                    updateSolvedQuotesTextViewVisibility(it)
                    updateContainer(it)
                }
            }
        }

        updateSolvedQuotesTextViewVisibility(emptyList()) // todo it is ugly and not clear

        return b.root
    }

    private fun updateContainer(newList: List<SolvedWithQuote>) {
        b.container.removeAllViews()
        for(it in newList) {
            b.container.addView(createRow(it))
        }
    }

    private fun updateSolvedQuotesTextViewVisibility(newList: List<SolvedWithQuote>) {
        b.solvedQuotesTextView.visibility = if(newList.isEmpty()) View.GONE else View.VISIBLE
    }

    @SuppressLint("InflateParams")
    private fun createRow(sqWithQ: SolvedWithQuote) =
        li.inflate(R.layout.solved_quote_row, null).apply {
            findViewById<Button>(R.id.text).apply {
                text = shortTextBeautifully(sqWithQ.quote.quote, 70)
                setOnClickListener {
                    onSolvedSelected(sqWithQ)
                }
            }
        }

    companion object {
        fun newInstance() = AllSolvedQuotesListFragment()
    }
}