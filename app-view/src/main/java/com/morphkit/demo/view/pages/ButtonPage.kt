package com.morphkit.demo.view.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.morphkit.demo.view.R
import com.morphkit.widget.button.MorphButton
import com.morphkit.widget.button.MorphRadioButton

class ButtonPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val density = context.resources.displayMetrics.density
        val dp16 = (16 * density).toInt()
        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16, dp16, dp16, dp16)
        }

        layout.addView(TextView(context).apply {
            text = getString(R.string.button_title)
            textSize = 22f
            setPadding(0, 0, 0, dp16)
        })

        // Primary button
        layout.addView(TextView(context).apply { text = getString(R.string.button_primary) })
        val primaryBtn = MorphButton(context).apply {
            text = getString(R.string.button_primary_text)
            setOnClickListener {
                Toast.makeText(context, getString(R.string.button_primary_toast), Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(primaryBtn, matchWrapParams(top = dp16, bottom = dp16))

        // Plain button
        layout.addView(TextView(context).apply { text = getString(R.string.button_plain) })
        val plainBtn = MorphButton(context).apply {
            text = getString(R.string.button_plain_text)
            style = MorphButton.Style.PLAIN
            setOnClickListener {
                Toast.makeText(context, getString(R.string.button_plain_toast), Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(plainBtn, matchWrapParams(top = dp16, bottom = dp16))

        // Disabled button
        layout.addView(TextView(context).apply { text = getString(R.string.button_disabled) })
        val disabledBtn = MorphButton(context).apply {
            text = getString(R.string.button_disabled_text)
            isEnabled = false
        }
        layout.addView(disabledBtn, matchWrapParams(top = dp16, bottom = dp16))

        // MorphRadioButton
        layout.addView(TextView(context).apply {
            text = getString(R.string.button_radio_title)
            textSize = 22f
            setPadding(0, dp16, 0, dp16)
        })
        val radioGroup = RadioGroup(context).apply {
            orientation = RadioGroup.VERTICAL
        }
        val radio1 = MorphRadioButton(context).apply { text = getString(R.string.button_option_a) }
        val radio2 = MorphRadioButton(context).apply { text = getString(R.string.button_option_b) }
        radioGroup.addView(radio1)
        radioGroup.addView(radio2)
        layout.addView(radioGroup)

        scrollView.addView(layout)
        return scrollView
    }

    private fun matchWrapParams(top: Int = 0, bottom: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = top; bottomMargin = bottom }
}
