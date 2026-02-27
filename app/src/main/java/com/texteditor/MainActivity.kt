package com.texteditor

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.texteditor.databinding.ActivityMainBinding
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentUri: Uri? = null
    private var currentType = FileType.TXT
    private var isApplyingHighlight = false

    private val openDocumentLauncher = registerForActivityResult(OpenDocument()) { uri ->
        if (uri != null) {
            currentUri = uri
            val text = readText(uri)
            binding.editorEditText.setText(text)
            currentType = inferTypeFromUri(uri)
            binding.fileTypeSpinner.setSelection(currentType.ordinal)
            applySyntaxHighlighting()
            showMessage("File loaded")
        }
    }

    private val createDocumentLauncher = registerForActivityResult(CreateDocument("text/plain")) { uri ->
        if (uri != null) {
            currentUri = uri
            saveToUri(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupListeners()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.file_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.fileTypeSpinner.adapter = adapter
        binding.fileTypeSpinner.setSelection(currentType.ordinal)
        binding.fileTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentType = FileType.entries[position]
                applySyntaxHighlighting()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupListeners() {
        binding.openButton.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("text/plain", "application/json", "application/xml", "text/xml", "*/*"))
        }

        binding.saveButton.setOnClickListener {
            val uri = currentUri
            if (uri != null) {
                saveToUri(uri)
            } else {
                createDocumentLauncher.launch(defaultFileName())
            }
        }

        binding.editorEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (!isApplyingHighlight) {
                    applySyntaxHighlighting()
                }
            }
        })
    }

    private fun applySyntaxHighlighting() {
        val current = binding.editorEditText.text?.toString().orEmpty()
        if (current.isEmpty()) return

        val selStart = binding.editorEditText.selectionStart
        val selEnd = binding.editorEditText.selectionEnd

        isApplyingHighlight = true
        val highlighted = SyntaxHighlighter.highlight(this, current, currentType)
        binding.editorEditText.text.replace(0, binding.editorEditText.length(), highlighted)
        val safeStart = min(selStart, binding.editorEditText.length())
        val safeEnd = min(selEnd, binding.editorEditText.length())
        binding.editorEditText.setSelection(safeStart, safeEnd)
        isApplyingHighlight = false
    }

    private fun saveToUri(uri: Uri) {
        val content = binding.editorEditText.text?.toString().orEmpty()
        contentResolver.openOutputStream(uri, "wt")?.bufferedWriter()?.use { writer ->
            writer.write(content)
        }
        showMessage("Saved")
    }

    private fun readText(uri: Uri): String {
        return contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
    }

    private fun inferTypeFromUri(uri: Uri): FileType {
        val name = queryDisplayName(uri)?.lowercase().orEmpty()
        return when {
            name.endsWith(".json") -> FileType.JSON
            name.endsWith(".xml") -> FileType.XML
            else -> FileType.TXT
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    return cursor.getString(index)
                }
            }
        }
        return null
    }

    private fun defaultFileName(): String {
        return when (currentType) {
            FileType.TXT -> "document.txt"
            FileType.JSON -> "document.json"
            FileType.XML -> "document.xml"
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
