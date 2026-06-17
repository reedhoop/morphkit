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
import com.morphkit.widget.selection.MorphCheckBox
import com.morphkit.widget.button.MorphRadioButton
import com.morphkit.theme.MorphTokens

class SelectionPage : Fragment() {

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

        // MorphCheckBox
        layout.addView(TextView(context).apply {
            text = "MorphCheckBox"
            textSize = 22f
            setPadding(0, 0, 0, MorphTokens.Spacing.spacingBase)
        })

        val check1 = MorphCheckBox(context).apply {
            text = "Enable notifications"
            setOnCheckedChangeListener { _, isChecked ->
                Toast.makeText(context, "Notifications: $isChecked", Toast.LENGTH_SHORT).show()
            }
        }
        val check2 = MorphCheckBox(context).apply {
            text = "Dark mode"
            isChecked = true
        }
        val check3 = MorphCheckBox(context).apply {
            text = "Auto-update"
        }
        layout.addView(check1)
        layout.addView(check2)
        layout.addView(check3)

        // MorphRadioButton
        layout.addView(TextView(context).apply {
            text = "MorphRadioButton"
            textSize = 22f
            setPadding(0, MorphTokens.Spacing.spacingBase, 0, MorphTokens.Spacing.spacingBase)
        })

        val radioGroup = RadioGroup(context).apply {
            orientation = RadioGroup.VERTICAL
        }
        val radio1 = MorphRadioButton(context).apply {
            text = "Option A"
            isChecked = true
        }
        val radio2 = MorphRadioButton(context).apply {
            text = "Option B"
        }
        val radio3 = MorphRadioButton(context).apply {
            text = "Option C"
        }
        radioGroup.addView(radio1)
        radioGroup.addView(radio2)
        radioGroup.addView(radio3)
        layout.addView(radioGroup)

        scrollView.addView(layout)
        return scrollView
    }
}
