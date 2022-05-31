package com.example.w_rds_pp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout


class SelectCategoryFragment private constructor(): Fragment() {
    private lateinit var categories: List<String>

    /** null means all categories **/
    var onCategorySelected: (String?) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categories = it.getStringArrayList(ARG_CATEGORIES_BUNDLE_NAME)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_select_category, container, false)

        v.findViewById<Button>(R.id.any_category_btn).setOnClickListener { onCategorySelected(null) }

        val catContainer: LinearLayout = v.findViewById(R.id.categories_container)
        categories.map(::createRow)
                  .forEach { catContainer.addView(it) }

        return v
    }

    @SuppressLint("InflateParams")
    private fun createRow(categoryName: String): View {
        val li: LayoutInflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = li.inflate(R.layout.category_selection_row_template, null)
        v.findViewById<Button>(R.id.category_button).apply {
            setOnClickListener { onCategorySelected(categoryName) }
            text = categoryName
        }
        return v.findViewById(R.id.row)
    }

    companion object {
        const val ARG_CATEGORIES_BUNDLE_NAME = "cat"
        fun newInstance(categories: List<String>) =
            SelectCategoryFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_CATEGORIES_BUNDLE_NAME, ArrayList(categories))
                }
            }
    }
}