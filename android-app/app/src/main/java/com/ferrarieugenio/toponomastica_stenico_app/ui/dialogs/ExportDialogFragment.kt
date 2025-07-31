package com.ferrarieugenio.toponomastica_stenico_app.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.ferrarieugenio.toponomastica_stenico_app.R
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import com.ferrarieugenio.toponomastica_stenico_app.databinding.DialogExportBinding
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.ExportFormat
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.ExporterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExportDialogFragment(
    private val toponyms: List<Toponym>,
    private val toponymRepository: ToponymRepository? = null,
    private val onExported: (exportedData: ByteArray, format: ExportFormat, fileName: String?) -> Unit
) : DialogFragment() {

    private var _binding: DialogExportBinding? = null
    private val binding get() = _binding!!

    private var singleMode: Boolean = false

    // secondary constructor for single-element export
    constructor(
        toponym: Toponym,
        toponymRepository: ToponymRepository? = null,
        onExported: (exportedData: ByteArray, format: ExportFormat, fileName: String?) -> Unit
    ) : this(listOf(toponym), toponymRepository, onExported) {
        singleMode = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(singleMode){
            binding.dialogTitle.text = "Condividi toponimo"
        }

        setupExportFormats()

        binding.cancelExportButton.setOnClickListener {
            dismiss()
        }

        binding.exportButton.setOnClickListener {
            val selectedFormat = getSelectedExportFormat()
            if (selectedFormat == null) {
                binding.filenameInputLayout.error = "Seleziona un formato"
                return@setOnClickListener
            }
            binding.filenameInputLayout.error = null

            setLoading(true)

            lifecycleScope.launch {
                val exportedStringResult = withContext(Dispatchers.Default) {
                    try {
                        val exporter = ExporterFactory.getExporter(selectedFormat, toponymRepository)

                        if (singleMode) {
                            exporter.export(toponyms[0])    // custom exporter if only element
                        } else {
                            exporter.export(toponyms)
                        }
                    } catch (ex: IllegalArgumentException) {
                        null // handle error below
                    }
                }

                if (exportedStringResult == null) {
                    binding.filenameInputLayout.error = "Errore durante l'esportazione"
                    setLoading(false)
                    return@launch
                }

                val rawFileName = binding.filenameEditText.text?.toString()?.trim()
                val finalFileName = rawFileName.takeIf { it?.isNotEmpty() == true }

                onExported(exportedStringResult, selectedFormat, finalFileName)

                setLoading(false)
                dismiss()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE

        binding.exportButton.isEnabled = !isLoading
        binding.cancelExportButton.isEnabled = !isLoading

        binding.filenameEditText.isEnabled = !isLoading
        for (i in 0 until binding.exportFormatRadioGroup.childCount) {
            binding.exportFormatRadioGroup.getChildAt(i).isEnabled = !isLoading
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    private fun setupExportFormats() {
        val radioGroup = binding.exportFormatRadioGroup
        radioGroup.removeAllViews()
        ExportFormat.entries.forEachIndexed { index, format ->
            val radioButton = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = "${format.displayName} (.${format.fileExtension})"
                tag = format
            }
            radioGroup.addView(radioButton)

            if (index == 0) {
                radioButton.isChecked = true
            }
        }
    }

    private fun getSelectedExportFormat(): ExportFormat? {
        val checkedId = binding.exportFormatRadioGroup.checkedRadioButtonId
        val radioButton = binding.exportFormatRadioGroup.findViewById<RadioButton>(checkedId)
        return radioButton?.tag as? ExportFormat
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
