package com.morphkit.demo.view.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.morphkit.demo.view.R
import com.morphkit.demo.view.dp
import com.morphkit.widget.container.MorphCardView
import com.morphkit.widget.text.MorphTextView

class CardPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(16))
        }

        layout.addView(MorphTextView(context).apply {
            text = getString(R.string.card_title)
            textSize = 22f
            setPadding(0, 0, 0, context.dp(16))
        })

        // Standard card
        layout.addView(MorphTextView(context).apply { text = getString(R.string.card_standard) })
        val standardCard = MorphCardView(context).apply {
            val inner = MorphTextView(context).apply {
                text = getString(R.string.card_standard_text)
                setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(16))
            }
            addView(inner)
        }
        layout.addView(standardCard, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = context.dp(8); bottomMargin = context.dp(16) })

        // Glassmorphism card
        layout.addView(MorphTextView(context).apply { text = getString(R.string.card_glass) })
        val glassCard = MorphCardView(context).apply {
            isGlassmorphism = true
            val inner = MorphTextView(context).apply {
                text = getString(R.string.card_glass_text)
                setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(16))
            }
            addView(inner)
        }
        layout.addView(glassCard, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = context.dp(8); bottomMargin = context.dp(16) })

        scrollView.addView(layout)
        return scrollView
    }
}
