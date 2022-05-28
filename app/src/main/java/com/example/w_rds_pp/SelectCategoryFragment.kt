package com.example.w_rds_pp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout


class SelectCategoryFragment : Fragment() {
    private lateinit var categories: List<String>

    /** null means all categories **/
    var onCategorySelected: (String?) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categories = it.getStringArrayList(CATEGORIES_BUNDLE_NAME)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_select_category, container, false)

        val allBtn = createCategoryButtonInstance("Any")
        allBtn.setOnClickListener { onCategorySelected(null) }
        allBtn.setBackgroundColor(Color.MAGENTA)
        allBtn.textSize = 26f
        val buttons: List<Button> = listOf(allBtn) + categories.map(::createCategoryButtonInstance)

        val catContainer: LinearLayout = v.findViewById(R.id.categories_container)
        buttons.forEach { catContainer.addView(it) }

        // Inflate the layout for this fragment
        return v
    }

    private fun createCategoryButtonInstance(categoryName: String): Button {
        val li: LayoutInflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = li.inflate(R.layout.select_category__category_button_template, null);
        val templateButton: Button = v.findViewById(R.id.category_button_template)
        return templateButton.apply {
            text = categoryName
            setOnClickListener { onCategorySelected(categoryName) }
        }
    }

    companion object {
        const val CATEGORIES_BUNDLE_NAME = "cat"
        @JvmStatic
        fun newInstance(categories: List<String>) =
            SelectCategoryFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(CATEGORIES_BUNDLE_NAME, ArrayList(categories))
                }
            }
    }
}