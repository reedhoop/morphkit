package com.morphkit.demo.view.pages

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.morphkit.demo.view.R
import com.morphkit.demo.view.dp
import com.morphkit.widget.text.MorphEditText
import com.morphkit.widget.text.MorphTextView

class EditTextPage : Fragment() {

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
            text = getString(R.string.edit_title)
            textSize = 22f
            setPadding(0, 0, 0, context.dp(16))
        })

        // Search style
        layout.addView(MorphTextView(context).apply { text = getString(R.string.edit_search) })
        val searchEdit = MorphEditText(context).apply {
            style = MorphEditText.Style.SEARCH
            hint = getString(R.string.edit_search_hint)
        }
        layout.addView(searchEdit, matchWrapParams(top = context.dp(8), bottom = context.dp(16)))

        // Bare style
        layout.addView(MorphTextView(context).apply { text = getString(R.string.edit_bare) })
        val bareEdit = MorphEditText(context).apply {
            style = MorphEditText.Style.BARE
            hint = getString(R.string.edit_bare_hint)
        }
        layout.addView(bareEdit, matchWrapParams(top = context.dp(8), bottom = context.dp(16)))

        // L17: inputType 演示 — 展示 MorphEditText 兼容各种输入类型
        layout.addView(MorphTextView(context).apply {
            text = getString(R.string.edit_password)
        })
        val passwordEdit = MorphEditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = getString(R.string.edit_password_hint)
        }
        layout.addView(passwordEdit, matchWrapParams(top = context.dp(8), bottom = context.dp(16)))

        layout.addView(MorphTextView(context).apply { text = getString(R.string.edit_email) })
        val emailEdit = MorphEditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            hint = getString(R.string.edit_email_hint)
        }
        layout.addView(emailEdit, matchWrapParams(top = context.dp(8), bottom = context.dp(16)))

        layout.addView(MorphTextView(context).apply { text = getString(R.string.edit_number) })
        val numberEdit = MorphEditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = getString(R.string.edit_number_hint)
        }
        layout.addView(numberEdit, matchWrapParams(top = context.dp(8), bottom = context.dp(16)))

        scrollView.addView(layout)
        return scrollView
    }

    private fun matchWrapParams(top: Int = 0, bottom: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = top; bottomMargin = bottom }
}
