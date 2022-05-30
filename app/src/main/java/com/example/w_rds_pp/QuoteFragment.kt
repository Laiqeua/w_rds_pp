package com.example.w_rds_pp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.w_rds_pp.databinding.FragmentQuoteBinding

class QuoteFragment : Fragment() {
    private lateinit var quote: Quote

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            quote = GsonInstance.fromJson(  it.getString("quote"), Quote::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentQuoteBinding.inflate(inflater, container, false)
        binding.quote.text = quote.quote
        binding.category.text = quote.category
        return binding.root
    }

    companion object {
        fun newInstance(quote: Quote) =
            QuoteFragment().apply {
                arguments = Bundle().apply {
                    putString("quote", GsonInstance.toJson(quote))
                }
            }
    }
}