package com.morphkit.demo.view.pages

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.morphkit.core.MorphKit
import com.morphkit.core.StylePolicy
import com.morphkit.theme.MorphTokens
import com.morphkit.widget.button.MorphButton

class SettingsPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val dp16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16, dp16, dp16, dp16)
        }

        layout.addView(TextView(context).apply {
            text = "Settings"
            textSize = 22f
            setPadding(0, 0, 0, MorphTokens.Spacing.spacingBase)
        })

        // Style Policy
        layout.addView(TextView(context).apply {
            text = "Style Policy"
            textSize = 18f
            setPadding(0, 0, 0, 8)
        })

        val policies = listOf(
            "AUTO" to StylePolicy.AUTO,
            "IOS" to StylePolicy.IOS,
            "PIXEL" to StylePolicy.PIXEL,
        )

        policies.forEach { (label, policy) ->
            val btn = MorphButton(context).apply {
                text = "Switch to $label"
                setOnClickListener {
                    // StylePolicy can only be set during MorphKit.init(), which runs once.
                    // Display the selected policy as feedback.
                    Toast.makeText(context, "Style: $label (requires restart)", Toast.LENGTH_SHORT).show()
                }
            }
            layout.addView(btn, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8 })
        }

        // Status
        layout.addView(TextView(context).apply {
            text = "Status"
            textSize = 18f
            setPadding(0, MorphTokens.Spacing.spacingBase, 0, 8)
        })

        layout.addView(TextView(context).apply {
            text = "Initialized: ${MorphKit.isInitialized()}"
        })
        layout.addView(TextView(context).apply {
            text = "Theme ResId: ${MorphKit.getFinalThemeResId()}"
        })

        scrollView.addView(layout)
        return scrollView
    }
}
