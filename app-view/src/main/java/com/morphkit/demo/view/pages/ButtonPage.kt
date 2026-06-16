package com.morphkit.demo.view.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.morphkit.widget.button.MorphButton
import com.morphkit.theme.MorphTokens

class ButtonPage : Fragment() {

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
            text = "MorphButton"
            textSize = 22f
            setPadding(0, 0, 0, MorphTokens.Spacing.spacingBase)
        })

        // Primary button
        layout.addView(TextView(context).apply { text = "Primary:" })
        val primaryBtn = MorphButton(context).apply {
            text = "Primary Button"
            setOnClickListener { Toast.makeText(context, "Primary clicked", Toast.LENGTH_SHORT).show() }
        }
        layout.addView(primaryBtn, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 16; bottomMargin = MorphTokens.Spacing.spacingBase })

        // Plain button
        layout.addView(TextView(context).apply { text = "Plain:" })
        val plainBtn = MorphButton(context).apply {
            text = "Plain Button"
            style = MorphButton.Style.PLAIN
            setOnClickListener { Toast.makeText(context, "Plain clicked", Toast.LENGTH_SHORT).show() }
        }
        layout.addView(plainBtn, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 16; bottomMargin = MorphTokens.Spacing.spacingBase })

        // Disabled button
        layout.addView(TextView(context).apply { text = "Disabled:" })
        val disabledBtn = MorphButton(context).apply {
            text = "Disabled Button"
            isEnabled = false
        }
        layout.addView(disabledBtn, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 16; bottomMargin = MorphTokens.Spacing.spacingBase })

        // MorphRadioButton
        layout.addView(TextView(context).apply {
            text = "MorphRadioButton"
            textSize = 22f
            setPadding(0, MorphTokens.Spacing.spacingBase, 0, MorphTokens.Spacing.spacingBase)
        })
        val radio1 = com.morphkit.widget.button.MorphRadioButton(context).apply { text = "Option A" }
        val radio2 = com.morphkit.widget.button.MorphRadioButton(context).apply { text = "Option B" }
        layout.addView(radio1)
        layout.addView(radio2)

        scrollView.addView(layout)
        return scrollView
    }
}
