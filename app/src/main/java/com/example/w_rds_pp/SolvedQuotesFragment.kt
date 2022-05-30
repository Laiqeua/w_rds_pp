package com.example.w_rds_pp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SolvedQuotesFragment : Fragment() {
    private lateinit var quotesContainer: LinearLayout

    private lateinit var li: LayoutInflater

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_solved_quotes, container, false)
        li = inflater

        quotesContainer = v.findViewById(R.id.container)

        val db = AppsDatabase.instance(requireContext())
        lifecycleScope.launch(Dispatchers.IO) {
            val livedata = db.solvedQuoteDao()
                             .selectSolvedQuotesWithQuotes()
            lifecycleScope.launch(Dispatchers.Main) {
                livedata.observe(viewLifecycleOwner) { onListChanged(it) }
            }
        }

        return v
    }

    @SuppressLint("InflateParams")
    private fun onListChanged(newList: List<SolvedQuoteWithQuote>){
        quotesContainer.removeAllViews()
        for(it in newList) {
            quotesContainer.addView(createRow(it))
        }
    }

    private fun createRow(sqWithQ: SolvedQuoteWithQuote) = li.inflate(R.layout.solved_quote_row, null).apply {
        findViewById<TextView>(R.id.text).apply {
            text = shortTextBeautifully(sqWithQ.quote, 70)
        }
        findViewById<TextView>(R.id.time).apply {
            text = timerFormatter(sqWithQ.time)
        }
    }

    companion object {
        fun newInstance() = SolvedQuotesFragment()
    }
}