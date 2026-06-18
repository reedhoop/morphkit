package com.morphkit.demo.view.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.morphkit.demo.view.R

/**
 * M13 演示页：XML 布局零侵入替换展示。
 *
 * 本页是 MorphKit 核心卖点的可视化证明：
 * - 布局文件 [R.layout.fragment_xml_demo] 中只声明 Android 标准控件
 *   （TextView / Button / EditText / RadioButton / CheckBox），不引用任何
 *   com.morphkit.widget.* 类。
 * - 运行时 [com.morphkit.core.MorphFactory2] 通过 LayoutInflater.Factory2
 *   责任链拦截这些标签名，自动替换为对应的 Morph* 控件。
 * - 业务方 XML 零改动，即可获得 iOS 极简风 / Pixel 原生风的统一视觉与交互。
 *
 * 替换证据通过读取 `findViewById` 返回实例的实际类名展示，证明标准控件
 * 已被静默替换为 Morph* 控件。
 */
class XmlDemoPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // 关键：inflate 标准 XML 布局，MorphFactory2 在此过程中自动替换控件
        val view = inflater.inflate(R.layout.fragment_xml_demo, container, false)

        bindInteractions(view)
        showReplacementEvidence(view)

        return view
    }

    private fun bindInteractions(view: View) {
        // findViewById 返回的实际上是 Morph* 控件，但静态类型仍是标准控件，
        // 业务方代码完全无感知，无需 import 任何 Morph* 类。
        view.findViewById<Button>(R.id.xml_demo_button_primary).setOnClickListener {
            Toast.makeText(requireContext(), R.string.xml_demo_button_primary_toast, Toast.LENGTH_SHORT).show()
        }
        view.findViewById<Button>(R.id.xml_demo_button_plain).setOnClickListener {
            Toast.makeText(requireContext(), R.string.xml_demo_button_plain_toast, Toast.LENGTH_SHORT).show()
        }
        view.findViewById<RadioButton>(R.id.xml_demo_radio_a).setOnClickListener {
            Toast.makeText(requireContext(), R.string.xml_demo_radio_a_toast, Toast.LENGTH_SHORT).show()
        }
        view.findViewById<RadioButton>(R.id.xml_demo_radio_b).setOnClickListener {
            Toast.makeText(requireContext(), R.string.xml_demo_radio_b_toast, Toast.LENGTH_SHORT).show()
        }
        view.findViewById<CheckBox>(R.id.xml_demo_check_notifications).setOnCheckedChangeListener { _, checked ->
            val msg = getString(R.string.xml_demo_check_notifications_toast, checked.toString())
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
        view.findViewById<CheckBox>(R.id.xml_demo_check_dark_mode).setOnCheckedChangeListener { _, checked ->
            val msg = getString(R.string.xml_demo_check_dark_mode_toast, checked.toString())
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
        view.findViewById<EditText>(R.id.xml_demo_edit_default)
        view.findViewById<EditText>(R.id.xml_demo_edit_search)
    }

    /**
     * 读取各控件实际运行时类名，证明标准 XML 标签已被 MorphFactory2 替换为 Morph* 控件。
     */
    private fun showReplacementEvidence(view: View) {
        val evidence = buildString {
            appendLine(getString(R.string.xml_demo_evidence_header))
            appendLine()
            appendLine(getString(R.string.xml_demo_evidence_text, className(view, R.id.xml_demo_title)))
            appendLine(getString(R.string.xml_demo_evidence_button, className(view, R.id.xml_demo_button_primary)))
            appendLine(getString(R.string.xml_demo_evidence_edit, className(view, R.id.xml_demo_edit_default)))
            appendLine(getString(R.string.xml_demo_evidence_radio, className(view, R.id.xml_demo_radio_a)))
            appendLine(getString(R.string.xml_demo_evidence_check, className(view, R.id.xml_demo_check_notifications)))
        }
        view.findViewById<TextView>(R.id.xml_demo_evidence).text = evidence
    }

    private fun className(view: View, id: Int): String {
        val v = view.findViewById<View>(id) ?: return "(null)"
        // 简短类名：MorphTextView / MorphButton / MorphEditText / MorphRadioButton / MorphCheckBox
        val full = v.javaClass.name
        val dot = full.lastIndexOf('.')
        return if (dot >= 0) full.substring(dot + 1) else full
    }
}
