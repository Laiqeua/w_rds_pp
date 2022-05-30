package com.example.w_rds_pp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AllSolvedQuotesListFragment : Fragment() {
    private lateinit var quotesContainer: LinearLayout

    private lateinit var li: LayoutInflater

    var onSolvedSelected: (SolvedWithQuote) -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_all_solved_quotes_list, container, false)
        li = inflater

        quotesContainer = v.findViewById(R.id.container)

        val db = AppsDatabase.instance(requireContext())
        lifecycleScope.launch(Dispatchers.IO) {
            val liveData = db.dao().selectSolvedWithQuote()
            lifecycleScope.launch(Dispatchers.Main) {
                liveData.observe(viewLifecycleOwner) {
                    updateList(it)
                }
            }
        }

        return v
    }

    @SuppressLint("InflateParams")
    private fun updateList(newList: List<SolvedWithQuote>) {
        quotesContainer.removeAllViews()
        for(it in newList) {
            quotesContainer.addView(createRow(it))
        }
    }

    private fun createRow(sqWithQ: SolvedWithQuote) = li.inflate(R.layout.solved_quote_row, null).apply {
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