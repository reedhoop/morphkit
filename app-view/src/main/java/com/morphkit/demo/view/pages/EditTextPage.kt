package com.morphkit.demo.view.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.morphkit.widget.text.MorphEditText
import com.morphkit.theme.MorphTokens

class EditTextPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        layout.addView(TextView(context).apply {
            text = "MorphEditText"
            textSize = 22f
            setPadding(0, 0, 0, MorphTokens.Spacing.spacingBase)
        })

        // Default style
        layout.addView(TextView(context).apply { text = "Default:" })
        val defaultEdit = MorphEditText(context).apply {
            hint = "Enter text..."
        }
        layout.addView(defaultEdit, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 8; bottomMargin = MorphTokens.Spacing.spacingBase })

        // Search style
        layout.addView(TextView(context).apply { text = "Search:" })
        val searchEdit = MorphEditText(context).apply {
            style = MorphEditText.Style.SEARCH
            hint = "Search..."
        }
        layout.addView(searchEdit, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 8; bottomMargin = MorphTokens.Spacing.spacingBase })

        // Bare style
        layout.addView(TextView(context).apply { text = "Bare:" })
        val bareEdit = MorphEditText(context).apply {
            style = MorphEditText.Style.BARE
            hint = "Bare input..."
        }
        layout.addView(bareEdit, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 8; bottomMargin = MorphTokens.Spacing.spacingBase })

        scrollView.addView(layout)
        return scrollView
    }
}
