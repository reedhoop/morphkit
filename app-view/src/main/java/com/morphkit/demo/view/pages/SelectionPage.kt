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
import com.morphkit.widget.selection.MorphCheckBox
import com.morphkit.widget.button.MorphRadioButton

class SelectionPage : Fragment() {

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

        // MorphCheckBox
        layout.addView(TextView(context).apply {
            text = getString(R.string.selection_check_title)
            textSize = 22f
            setPadding(0, 0, 0, dp16)
        })

        val check1 = MorphCheckBox(context).apply {
            text = getString(R.string.selection_check_notifications)
            setOnCheckedChangeListener { _, isChecked ->
                Toast.makeText(
                    context,
                    getString(R.string.selection_check_notifications_toast, isChecked),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val check2 = MorphCheckBox(context).apply {
            text = getString(R.string.selection_check_dark_mode)
            isChecked = true
        }
        val check3 = MorphCheckBox(context).apply {
            text = getString(R.string.selection_check_auto_update)
        }
        layout.addView(check1)
        layout.addView(check2)
        layout.addView(check3)

        // MorphRadioButton
        layout.addView(TextView(context).apply {
            text = getString(R.string.selection_radio_title)
            textSize = 22f
            setPadding(0, dp16, 0, dp16)
        })

        val radioGroup = RadioGroup(context).apply {
            orientation = RadioGroup.VERTICAL
        }
        val radio1 = MorphRadioButton(context).apply {
            text = getString(R.string.selection_option_a)
            isChecked = true
        }
        val radio2 = MorphRadioButton(context).apply {
            text = getString(R.string.selection_option_b)
        }
        val radio3 = MorphRadioButton(context).apply {
            text = getString(R.string.selection_option_c)
        }
        radioGroup.addView(radio1)
        radioGroup.addView(radio2)
        radioGroup.addView(radio3)
        layout.addView(radioGroup)

        scrollView.addView(layout)
        return scrollView
    }
}
