package com.morphkit.demo.view.pages

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.morphkit.widget.container.MorphCardView

class CardPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val dp16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
        val dp8 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).toInt()
        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16, dp16, dp16, dp16)
        }

        layout.addView(TextView(context).apply {
            text = "MorphCardView"
            textSize = 22f
            setPadding(0, 0, 0, dp16)
        })

        // Standard card
        layout.addView(TextView(context).apply { text = "Standard Card:" })
        val standardCard = MorphCardView(context).apply {
            val inner = TextView(context).apply {
                text = "This is a standard MorphCardView with theme styling."
                setPadding(dp16, dp16, dp16, dp16)
            }
            addView(inner)
        }
        layout.addView(standardCard, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp8; bottomMargin = dp16 })

        // Glassmorphism card
        layout.addView(TextView(context).apply { text = "Glassmorphism Card:" })
        val glassCard = MorphCardView(context).apply {
            isGlassmorphism = true
            val inner = TextView(context).apply {
                text = "This is a glassmorphism MorphCardView with backdrop blur."
                setPadding(dp16, dp16, dp16, dp16)
            }
            addView(inner)
        }
        layout.addView(glassCard, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp8; bottomMargin = dp16 })

        scrollView.addView(layout)
        return scrollView
    }
}
