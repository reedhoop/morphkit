package com.morphkit.demo.view.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.morphkit.demo.view.R
import com.morphkit.widget.text.MorphTextView

class TextPage : Fragment() {

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
            text = getString(R.string.text_title)
            textSize = 22f
            setPadding(0, 0, 0, dp16)
        })

        val morphText = MorphTextView(context).apply {
            text = getString(R.string.text_hello)
        }
        layout.addView(morphText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp16 })

        layout.addView(TextView(context).apply {
            text = getString(R.string.text_description)
            textSize = 14f
        })

        scrollView.addView(layout)
        return scrollView
    }
}
