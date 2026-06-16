package com.morphkit.demo.view.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.morphkit.widget.container.MorphCardView
import com.morphkit.theme.MorphTokens

class CardPage : Fragment() {

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
            text = "MorphCardView"
            textSize = 22f
            setPadding(0, 0, 0, MorphTokens.Spacing.spacingBase)
        })

        // Standard card
        layout.addView(TextView(context).apply { text = "Standard Card:" })
        val standardCard = MorphCardView(context).apply {
            val inner = TextView(context).apply {
                text = "This is a standard MorphCardView with theme styling."
                setPadding(24, 24, 24, 24)
            }
            addView(inner)
        }
        layout.addView(standardCard, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 8; bottomMargin = MorphTokens.Spacing.spacingBase })

        // Glassmorphism card
        layout.addView(TextView(context).apply { text = "Glassmorphism Card:" })
        val glassCard = MorphCardView(context).apply {
            isGlassmorphism = true
            val inner = TextView(context).apply {
                text = "This is a glassmorphism MorphCardView with backdrop blur."
                setPadding(24, 24, 24, 24)
            }
            addView(inner)
        }
        layout.addView(glassCard, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 8; bottomMargin = MorphTokens.Spacing.spacingBase })

        scrollView.addView(layout)
        return scrollView
    }
}
