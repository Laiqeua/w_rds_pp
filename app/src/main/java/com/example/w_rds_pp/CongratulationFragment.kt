package com.example.w_rds_pp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class CongratulationFragment : Fragment() {
    private var text: String? = null

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            text = it.getString("text")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_congratulation, container, false)
        textView = v.findViewById(R.id.quote)
        textView.text = text
        return v
    }

    companion object {
        @JvmStatic
        fun newInstance(text: String) =
            CongratulationFragment().apply {
                arguments = Bundle().apply {
                    putString("text", text)
                }
            }
    }
}