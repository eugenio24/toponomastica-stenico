package com.ferrarieugenio.toponomastica_stenico_app.ui.dialogs

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import com.ferrarieugenio.toponomastica_stenico_app.databinding.DialogMapStyleSelectorBinding
import com.ferrarieugenio.toponomastica_stenico_app.util.map.MapStyle
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MapStyleSelectorDialog(
    private val context: Context,
    private val currentStyle: MapStyle,
    private val onStyleSelected: (MapStyle) -> Unit
) {
    fun show() {
        val binding = DialogMapStyleSelectorBinding.inflate(LayoutInflater.from(context))

        fun resolveAttrColor(attr: Int): Int {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(attr, typedValue, true)
            return typedValue.data
        }

        var styleType: MapStyle = currentStyle
        var showContours = currentStyle.showContours
        var showMunicipalities = currentStyle.showMunicipalities

        val selectedColor = resolveAttrColor(com.google.android.material.R.attr.colorPrimary)
        val unselectedStrokeColor = resolveAttrColor(com.google.android.material.R.attr.colorOutline)
        val unselectedTextColor = resolveAttrColor(com.google.android.material.R.attr.colorOnSurfaceVariant)

        fun updateUI() {
            val isOsm = styleType is MapStyle.OSM

            val (selectedCard, selectedText) = if (isOsm) {
                binding.styleOptionOsm to binding.textOsm
            } else {
                binding.styleOptionSatellite to binding.textSatellite
            }

            val (unselectedCard, unselectedText) = if (isOsm) {
                binding.styleOptionSatellite to binding.textSatellite
            } else {
                binding.styleOptionOsm to binding.textOsm
            }

            selectedCard.strokeColor = selectedColor
            selectedText.setTextColor(selectedColor)

            unselectedCard.strokeColor = unselectedStrokeColor
            unselectedText.setTextColor(unselectedTextColor)

            binding.contoursToggle.isChecked = showContours
            binding.municipalitiesToggle.isChecked = showMunicipalities
        }

        fun applySelection(
            newStyleType: MapStyle? = null,
            newContours: Boolean? = null,
            newMunicipalities: Boolean? = null
        ) {
            newStyleType?.let { styleType = it }
            newContours?.let { showContours = it }
            newMunicipalities?.let { showMunicipalities = it }

            styleType = when (styleType) {
                is MapStyle.OSM -> MapStyle.OSM(showContours, showMunicipalities)
                is MapStyle.SATELLITE -> MapStyle.SATELLITE(showContours, showMunicipalities)
            }

            updateUI()
        }

        applySelection()

        binding.styleOptionOsm.setOnClickListener {
            applySelection(newStyleType = MapStyle.OSM(showContours, showMunicipalities))
        }

        binding.styleOptionSatellite.setOnClickListener {
            applySelection(newStyleType = MapStyle.SATELLITE(showContours, showMunicipalities))
        }

        binding.contoursToggle.setOnCheckedChangeListener { _, isChecked ->
            applySelection(newContours = isChecked)
        }

        binding.municipalitiesToggle.setOnCheckedChangeListener { _, isChecked ->
            applySelection(newMunicipalities = isChecked)
        }

        MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setNegativeButton("Annulla", null)
            .setPositiveButton("Conferma") { _, _ ->
                if (styleType != currentStyle) {
                    onStyleSelected(styleType)
                }
            }
            .show()
    }
}