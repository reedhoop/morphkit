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
import com.morphkit.widget.selection.MorphCheckBox
import com.morphkit.widget.button.MorphRadioButton
import com.morphkit.theme.MorphTokens

class SelectionPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
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
        layout.addView(radio1)
        layout.addView(radio2)
        layout.addView(radio3)

        scrollView.addView(layout)
        return scrollView
    }
}
