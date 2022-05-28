package com.example.w_rds_pp

import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout


class SelectCategoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
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

        val allBtn = createCategoryButton("Any")
        allBtn.setOnClickListener { onCategorySelected(null) }
        allBtn.setBackgroundColor(Color.MAGENTA)
        allBtn.textSize = 26f
        val buttons: List<Button> = listOf(allBtn) + categories.map(::createCategoryButton)

        val catContainer: LinearLayout = v.findViewById(R.id.categories_container)
        buttons.forEach { catContainer.addView(it) }

        // Inflate the layout for this fragment
        return v
    }

    private fun createCategoryButton(categoryName: String): Button {
        val button = Button(requireContext())
        button.textSize = 22f
        button.text = categoryName
        button.setOnClickListener { onCategorySelected(categoryName) }
        return button
    }

    companion object {
        private const val CATEGORIES_BUNDLE_NAME = "cat"
        @JvmStatic
        fun newInstance(categories: List<String>) =
            SelectCategoryFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(CATEGORIES_BUNDLE_NAME, ArrayList(categories))
                }
            }
    }
}