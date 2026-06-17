package com.morphkit.demo.view.pages

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.morphkit.demo.view.R

class WidgetCatalogPage : Fragment() {

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

        val title = TextView(context).apply {
            text = "MorphKit Widgets"
            textSize = 24f
            setPadding(0, 0, 0, dp16)
        }
        layout.addView(title)

        val items = listOf(
            "MorphButton" to R.id.action_catalog_to_button,
            "MorphTextView" to R.id.action_catalog_to_text,
            "MorphEditText" to R.id.action_catalog_to_editText,
            "MorphCardView" to R.id.action_catalog_to_card,
            "RadioButton & CheckBox" to R.id.action_catalog_to_selection,
            "Settings" to R.id.action_catalog_to_settings,
        )

        items.forEach { (label, navId) ->
            val item = TextView(context).apply {
                text = "\u25B8 $label"
                textSize = 18f
                setPadding(0, dp16, 0, dp16)
                setOnClickListener { findNavController().navigate(navId) }
            }
            layout.addView(item)
        }

        scrollView.addView(layout)
        return scrollView
    }
}
