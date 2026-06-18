package com.morphkit.demo.view.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.morphkit.demo.view.R
import com.morphkit.demo.view.dp
import com.morphkit.widget.text.MorphTextView

class TextPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val scrollView = ScrollView(context)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(16))
        }

        // 控件功能演示页：显式构造 MorphTextView 以展示其 typography / 颜色能力。
        // 框架自动替换能力的展示见 XmlDemoPage（XML 布局 + MorphFactory2 责任链）。
        layout.addView(MorphTextView(context).apply {
            text = getString(R.string.text_title)
            textSize = 22f
            setPadding(0, 0, 0, context.dp(16))
        })

        val morphText = MorphTextView(context).apply {
            text = getString(R.string.text_hello)
        }
        layout.addView(morphText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = context.dp(16) })

        layout.addView(MorphTextView(context).apply {
            text = getString(R.string.text_description)
            textSize = 14f
        })

        scrollView.addView(layout)
        return scrollView
    }
}
