package com.morphkit.demo.view.pages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.morphkit.core.MorphKit
import com.morphkit.core.StylePolicy
import com.morphkit.demo.view.R
import com.morphkit.demo.view.dp
import com.morphkit.widget.button.MorphButton
import com.morphkit.widget.text.MorphTextView

class SettingsPage : Fragment() {

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
            text = getString(R.string.settings_title)
            textSize = 22f
            setPadding(0, 0, 0, context.dp(16))
        })

        // Style Policy
        layout.addView(MorphTextView(context).apply {
            text = getString(R.string.settings_style_policy)
            textSize = 18f
            setPadding(0, 0, 0, context.dp(8))
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
                    context.getSharedPreferences("morphkit_demo", Context.MODE_PRIVATE)
                        .edit()
                        .putString("pending_style_policy", policy.name)
                        .apply()
                    Toast.makeText(
                        context,
                        getString(R.string.settings_recorded_restart),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            layout.addView(btn, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = context.dp(8) })
        }

        // Status
        layout.addView(MorphTextView(context).apply {
            text = getString(R.string.settings_status)
            textSize = 18f
            setPadding(0, context.dp(16), 0, context.dp(8))
        })

        layout.addView(MorphTextView(context).apply {
            text = getString(R.string.settings_initialized, MorphKit.isInitialized())
        })
        layout.addView(MorphTextView(context).apply {
            text = getString(R.string.settings_theme_resid, if (MorphKit.isInitialized()) MorphKit.getFinalThemeResId() else 0)
        })

        scrollView.addView(layout)
        return scrollView
    }
}
