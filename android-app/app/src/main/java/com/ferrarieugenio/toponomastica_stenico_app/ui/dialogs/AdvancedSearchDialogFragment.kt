package com.ferrarieugenio.toponomastica_stenico_app.ui.dialogs

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.ferrarieugenio.toponomastica_stenico_app.R
import com.ferrarieugenio.toponomastica_stenico_app.databinding.DialogAdvancedSearchBinding
import com.ferrarieugenio.toponomastica_stenico_app.util.AdvancedFilters
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class AdvancedSearchDialogFragment(
    private val initialFilters: AdvancedFilters?,
    private val onApplyFilters: (AdvancedFilters) -> Unit,
    private val availableClusters: List<String>,
    private val availableHcClusters: List<String>,
    private val availableTags: List<String>
) : DialogFragment() {

    private var _binding: DialogAdvancedSearchBinding? = null
    private val binding get() = _binding!!

    private val selectedTags = mutableListOf<String>()
    private val selectedClusters = mutableListOf<String>()
    private val selectedHcClusters = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAdvancedSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeQuotaControls(initialFilters?.minQuota, initialFilters?.maxQuota)
        initialFilters?.let {
            selectedClusters.clear()
            selectedClusters.addAll(it.selectedClusters)
            updateClusterChips()

            selectedHcClusters.clear()
            selectedHcClusters.addAll(it.selectedHcClusters)
            updateClusterChips()

            selectedTags.clear()
            selectedTags.addAll(it.selectedTags)
            updateTagChips()
        }

        updateAllChips()
        updateVisibility()
        setupQuotaControls()

        binding.selectClustersButton.setOnClickListener { showMultiChoiceDialog(
                "Seleziona Sezione", availableClusters, selectedClusters) { selected ->
                selectedClusters.clear()
                selectedClusters.addAll(selected)
                updateClusterChips()
            }
        }

        binding.selectHcClustersButton.setOnClickListener { showMultiChoiceDialog(
                "Seleziona Zona", availableHcClusters, selectedHcClusters) { selected ->
                selectedHcClusters.clear()
                selectedHcClusters.addAll(selected)
                updateHcClusterChips()
            }
        }

        binding.selectTagsButton.setOnClickListener { showMultiChoiceDialog(
                "Seleziona tag", availableTags, selectedTags) { selected ->
                selectedTags.clear()
                selectedTags.addAll(selected)
                updateTagChips()
            }
        }

        binding.applyButton.setOnClickListener {
            val minQuota = binding.minQuotaEditText.text.toString().toIntOrNull()
                ?.takeIf { it != 0 }

            val maxQuota = binding.maxQuotaEditText.text.toString().toIntOrNull()
                ?.takeIf { it != 3500 }

            val filters = AdvancedFilters(
                minQuota = minQuota,
                maxQuota = maxQuota,
                selectedTags = selectedTags.toList(),
                selectedClusters = selectedClusters.toList(),
                selectedHcClusters = selectedHcClusters.toList()
            )
            onApplyFilters(filters)
            dismiss()
        }

        binding.removeAllFiltersButton.setOnClickListener {
            removeAllFilters()
            Toast.makeText(requireContext(), "Filtri rimossi", Toast.LENGTH_SHORT).show()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun updateAllChips() {
        updateTagChips()
        updateClusterChips()
        updateHcClusterChips()
    }

    private fun updateChips(
        chipGroup: ChipGroup,
        items: MutableList<String>
    ) {
        chipGroup.removeAllViews()
        items.forEach { item ->
            val chip = Chip(requireContext()).apply {
                text = item
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    items.remove(item)
                    updateChips(chipGroup, items)
                }
            }
            chipGroup.addView(chip)
        }
        updateVisibility()
    }

    private fun updateTagChips() {
        updateChips(binding.selectedTagsChipGroup, selectedTags)
    }

    private fun updateClusterChips() {
        updateChips(binding.selectedClustersChipGroup, selectedClusters)
    }

    private fun updateHcClusterChips() {
        updateChips(binding.selectedHcClustersChipGroup, selectedHcClusters)
    }


    private fun setGroupVisibility(items: List<String>, chipGroup: View, hintView: View) {
        if (items.isEmpty()) {
            hintView.visibility = View.VISIBLE
            chipGroup.visibility = View.GONE
        } else {
            hintView.visibility = View.GONE
            chipGroup.visibility = View.VISIBLE
        }
    }

    private fun updateVisibility() {
        setGroupVisibility(selectedTags, binding.selectedTagsChipGroup, binding.noTagsHint)
        setGroupVisibility(selectedClusters, binding.selectedClustersChipGroup, binding.noClusterHint)
        setGroupVisibility(selectedHcClusters, binding.selectedHcClustersChipGroup, binding.noHcClusterHint)
    }

    private fun showMultiChoiceDialog(
        title: String,
        options: List<String>,
        selectedItems: List<String>,
        onSelectedChanged: (List<String>) -> Unit
    ) {
        val selected = selectedItems.toMutableList()
        val checkedItems = options.map { selected.contains(it) }.toBooleanArray()
        AlertDialog.Builder(requireContext(), R.style.MyAlertDialogTheme)
            .setTitle(title)
            .setMultiChoiceItems(options.toTypedArray(), checkedItems) { _, which, isChecked ->
                if (isChecked) selected.add(options[which]) else selected.remove(options[which])
            }
            .setPositiveButton("OK") { _, _ -> onSelectedChanged(selected) }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun initializeQuotaControls(initialMinQuota: Int?, initialMaxQuota: Int?) {
        val minVal = initialMinQuota?.coerceIn(
            binding.quotaRangeSlider.valueFrom.toInt(),
            binding.quotaRangeSlider.valueTo.toInt()
        )  ?: 0
        val maxVal = initialMaxQuota?.coerceIn(
            binding.quotaRangeSlider.valueFrom.toInt(),
            binding.quotaRangeSlider.valueTo.toInt()
        ) ?: 3500

        binding.quotaRangeSlider.values = listOf(minVal.toFloat(), maxVal.toFloat())

        binding.minQuotaEditText.setText(minVal.toString())
        binding.maxQuotaEditText.setText(maxVal.toString())
    }

    private fun setupQuotaControls() {
        val slider = binding.quotaRangeSlider
        val minEdit = binding.minQuotaEditText
        val maxEdit = binding.maxQuotaEditText

        slider.addOnChangeListener { _, _, _ ->
            val (newMin, newMax) = slider.values
            if (minEdit.text.toString() != newMin.toInt().toString()) {
                minEdit.setText(newMin.toInt().toString())
            }
            if (maxEdit.text.toString() != newMax.toInt().toString()) {
                maxEdit.setText(newMax.toInt().toString())
            }
        }

        // Called on focus loss or done
        fun updateSliderFromEdits() {
            val minInput = minEdit.text.toString().toIntOrNull()
            val maxInput = maxEdit.text.toString().toIntOrNull()
            if (minInput != null && maxInput != null && minInput <= maxInput) {
                val newMin = minInput.coerceIn(slider.valueFrom.toInt(), slider.valueTo.toInt())
                val newMax = maxInput.coerceIn(slider.valueFrom.toInt(), slider.valueTo.toInt())
                slider.values = listOf(newMin.toFloat(), newMax.toFloat())
            }
        }

        minEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) updateSliderFromEdits()
        }
        minEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateSliderFromEdits()
                minEdit.clearFocus()
                true
            } else false
        }

        maxEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) updateSliderFromEdits()
        }
        maxEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateSliderFromEdits()
                maxEdit.clearFocus()
                true
            } else false
        }
    }

    private fun removeAllFilters() {
        binding.quotaRangeSlider.values = listOf(binding.quotaRangeSlider.valueFrom, binding.quotaRangeSlider.valueTo)
        binding.minQuotaEditText.setText(binding.quotaRangeSlider.valueFrom.toInt().toString())
        binding.maxQuotaEditText.setText(binding.quotaRangeSlider.valueTo.toInt().toString())

        selectedTags.clear()
        updateTagChips()

        selectedClusters.clear()
        updateClusterChips()

        selectedHcClusters.clear()
        updateHcClusterChips()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}
