package com.morphkit.demo.view.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.morphkit.widget.text.MorphTextView
import com.morphkit.theme.MorphTokens

class TextPage : Fragment() {

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
            text = "MorphTextView"
            textSize = 22f
            setPadding(0, 0, 0, MorphTokens.Spacing.spacingBase)
        })

        val morphText = MorphTextView(context).apply {
            text = "Hello MorphKit! This is a MorphTextView with automatic theme styling applied."
        }
        layout.addView(morphText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = MorphTokens.Spacing.spacingBase })

        layout.addView(TextView(context).apply {
            text = "MorphTextView supports the full MorphKit theme system, including typography tokens and color schemes."
            textSize = 14f
        })

        scrollView.addView(layout)
        return scrollView
    }
}
