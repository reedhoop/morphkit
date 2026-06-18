package com.morphkit.demo.view.pages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.morphkit.demo.view.R
import com.morphkit.demo.view.dp
import com.morphkit.widget.text.MorphTextView

class WidgetCatalogPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(16))
        }

        val title = MorphTextView(context).apply {
            text = getString(R.string.catalog_title)
            textSize = 24f
            setPadding(0, 0, 0, context.dp(16))
        }
        layout.addView(title)

        val items = listOf(
            getString(R.string.catalog_item_button) to R.id.action_catalog_to_button,
            getString(R.string.catalog_item_text) to R.id.action_catalog_to_text,
            getString(R.string.catalog_item_edit) to R.id.action_catalog_to_editText,
            getString(R.string.catalog_item_card) to R.id.action_catalog_to_card,
            getString(R.string.catalog_item_selection) to R.id.action_catalog_to_selection,
            getString(R.string.catalog_item_settings) to R.id.action_catalog_to_settings,
        )

        items.forEach { (label, navId) ->
            val item = MorphTextView(context).apply {
                text = "\u25B8 $label"
                textSize = 18f
                setPadding(0, context.dp(16), 0, context.dp(16))
                setOnClickListener {
                    try {
                        findNavController().navigate(navId)
                    } catch (t: Throwable) {
                        Log.e("WidgetCatalogPage", "navigate failed", t)
                    }
                }
            }
            layout.addView(item)
        }

        scrollView.addView(layout)
        return scrollView
    }
}
