package com.texteditor

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat

object SyntaxHighlighter {

    private val jsonStringRegex = Regex("\"(?:\\\\.|[^\\\"])*\"")
    private val jsonKeyRegex = Regex("\"(?:\\\\.|[^\\\"])*\"(?=\\s*:)")
    private val jsonNumberRegex = Regex("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?")
    private val jsonBooleanRegex = Regex("\\b(?:true|false|null)\\b")

    private val xmlCommentRegex = Regex("<!--.*?-->", setOf(RegexOption.DOT_MATCHES_ALL))
    private val xmlTagRegex = Regex("</?\\s*([A-Za-z_][\\w:.-]*)")
    private val xmlAttrNameRegex = Regex("\\s([A-Za-z_:][\\w:.-]*)(?=\\s*=)")
    private val xmlAttrValueRegex = Regex("\"[^\"]*\"|'[^']*'")

    fun highlight(context: Context, source: String, type: FileType): SpannableStringBuilder {
        val builder = SpannableStringBuilder(source)
        when (type) {
            FileType.TXT -> return builder
            FileType.JSON -> highlightJson(context, builder)
            FileType.XML -> highlightXml(context, builder)
        }
        return builder
    }

    private fun highlightJson(context: Context, builder: SpannableStringBuilder) {
        val keyColor = color(context, R.color.syntax_json_key)
        val stringColor = color(context, R.color.syntax_json_string)
        val numberColor = color(context, R.color.syntax_json_number)
        val booleanColor = color(context, R.color.syntax_json_boolean)

        applyColor(builder, jsonStringRegex, stringColor)
        applyColor(builder, jsonKeyRegex, keyColor)
        applyColor(builder, jsonNumberRegex, numberColor)
        applyColor(builder, jsonBooleanRegex, booleanColor)
    }

    private fun highlightXml(context: Context, builder: SpannableStringBuilder) {
        val commentColor = color(context, R.color.syntax_xml_comment)
        val tagColor = color(context, R.color.syntax_xml_tag)
        val attrNameColor = color(context, R.color.syntax_xml_attr_name)
        val attrValueColor = color(context, R.color.syntax_xml_attr_value)

        applyColor(builder, xmlCommentRegex, commentColor)
        applyColor(builder, xmlTagRegex, tagColor, group = 1)
        applyColor(builder, xmlAttrNameRegex, attrNameColor, group = 1)
        applyColor(builder, xmlAttrValueRegex, attrValueColor)
    }

    private fun applyColor(
        builder: SpannableStringBuilder,
        regex: Regex,
        color: Int,
        group: Int = 0
    ) {
        regex.findAll(builder).forEach { match ->
            val range = if (group == 0) {
                match.range
            } else {
                val groupRange = match.groups[group]?.range ?: return@forEach
                groupRange
            }
            builder.setSpan(
                ForegroundColorSpan(color),
                range.first,
                range.last + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun color(context: Context, id: Int): Int = ContextCompat.getColor(context, id)
}
