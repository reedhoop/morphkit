package com.morphkit.demo.view.pages

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.morphkit.widget.button.MorphButton
import com.morphkit.widget.button.MorphRadioButton

class ButtonPage : Fragment() {

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
            text = "MorphButton"
            textSize = 22f
            setPadding(0, 0, 0, dp16)
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
        ).apply { topMargin = dp16; bottomMargin = dp16 })

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
        ).apply { topMargin = dp16; bottomMargin = dp16 })

        // Disabled button
        layout.addView(TextView(context).apply { text = "Disabled:" })
        val disabledBtn = MorphButton(context).apply {
            text = "Disabled Button"
            isEnabled = false
        }
        layout.addView(disabledBtn, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp16; bottomMargin = dp16 })

        // MorphRadioButton
        layout.addView(TextView(context).apply {
            text = "MorphRadioButton"
            textSize = 22f
            setPadding(0, dp16, 0, dp16)
        })
        val radioGroup = RadioGroup(context).apply {
            orientation = RadioGroup.VERTICAL
        }
        val radio1 = MorphRadioButton(context).apply { text = "Option A" }
        val radio2 = MorphRadioButton(context).apply { text = "Option B" }
        radioGroup.addView(radio1)
        radioGroup.addView(radio2)
        layout.addView(radioGroup)

        scrollView.addView(layout)
        return scrollView
    }
}
