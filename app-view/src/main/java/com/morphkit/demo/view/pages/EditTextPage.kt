package com.morphkit.demo.view.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.morphkit.demo.view.R
import com.morphkit.widget.text.MorphEditText

class EditTextPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val density = context.resources.displayMetrics.density
        val dp16 = (16 * density).toInt()
        val dp8 = (8 * density).toInt()
        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16, dp16, dp16, dp16)
        }

        layout.addView(TextView(context).apply {
            text = getString(R.string.edit_title)
            textSize = 22f
            setPadding(0, 0, 0, dp16)
        })

        // Default style
        layout.addView(TextView(context).apply { text = getString(R.string.edit_default) })
        val defaultEdit = MorphEditText(context).apply {
            hint = getString(R.string.edit_default_hint)
        }
        layout.addView(defaultEdit, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp8; bottomMargin = dp16 })

        // Search style
        layout.addView(TextView(context).apply { text = getString(R.string.edit_search) })
        val searchEdit = MorphEditText(context).apply {
            style = MorphEditText.Style.SEARCH
            hint = getString(R.string.edit_search_hint)
        }
        layout.addView(searchEdit, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp8; bottomMargin = dp16 })

        // Bare style
        layout.addView(TextView(context).apply { text = getString(R.string.edit_bare) })
        val bareEdit = MorphEditText(context).apply {
            style = MorphEditText.Style.BARE
            hint = getString(R.string.edit_bare_hint)
        }
        layout.addView(bareEdit, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp8; bottomMargin = dp16 })

        scrollView.addView(layout)
        return scrollView
    }
}
