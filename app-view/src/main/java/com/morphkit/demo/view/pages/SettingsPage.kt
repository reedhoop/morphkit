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
import com.morphkit.core.MorphKit
import com.morphkit.core.StylePolicy
import com.morphkit.demo.view.R
import com.morphkit.widget.button.MorphButton

class SettingsPage : Fragment() {

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
            text = getString(R.string.settings_title)
            textSize = 22f
            setPadding(0, 0, 0, dp16)
        })

        // Style Policy
        layout.addView(TextView(context).apply {
            text = getString(R.string.settings_style_policy)
            textSize = 18f
            setPadding(0, 0, 0, dp8)
        })

        val policies = listOf(
            "AUTO" to StylePolicy.AUTO,
            "IOS" to StylePolicy.IOS,
            "PIXEL" to StylePolicy.PIXEL,
        )

        policies.forEach { (label, policy) ->
            val btn = MorphButton(context).apply {
                text = getString(R.string.settings_switch_to, label)
                setOnClickListener {
                    // StylePolicy can only be set during MorphKit.init(), which runs once.
                    // Display the selected policy as feedback.
                    Toast.makeText(
                        context,
                        getString(R.string.settings_requires_restart, label),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            layout.addView(btn, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp8 })
        }

        // Status
        layout.addView(TextView(context).apply {
            text = getString(R.string.settings_status)
            textSize = 18f
            setPadding(0, dp16, 0, dp8)
        })

        layout.addView(TextView(context).apply {
            text = getString(R.string.settings_initialized, MorphKit.isInitialized())
        })
        layout.addView(TextView(context).apply {
            text = getString(R.string.settings_theme_resid, if (MorphKit.isInitialized()) MorphKit.getFinalThemeResId() else 0)
        })

        scrollView.addView(layout)
        return scrollView
    }
}
