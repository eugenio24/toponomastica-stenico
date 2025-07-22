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

        var currentSelection = currentStyle

        binding.contoursToggle.isChecked = currentStyle.showContours

        fun updateSelection(selected: MapStyle) {
            val typedValue = TypedValue()
            val theme = context.theme

            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            val selectedColor = typedValue.data

            theme.resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue, true)
            val unselectedStrokeColor = typedValue.data

            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true)
            val unselectedTextColor = typedValue.data

            val selectedViews = when (selected) {
                is MapStyle.OSM -> binding.styleOptionOsm to binding.textOsm
                is MapStyle.SATELLITE -> binding.styleOptionSatellite to binding.textSatellite
            }

            val unselectedViews = when (selected) {
                is MapStyle.OSM -> binding.styleOptionSatellite to binding.textSatellite
                is MapStyle.SATELLITE -> binding.styleOptionOsm to binding.textOsm
            }

            selectedViews.first.strokeColor = selectedColor
            selectedViews.second.setTextColor(selectedColor)

            unselectedViews.first.strokeColor = unselectedStrokeColor
            unselectedViews.second.setTextColor(unselectedTextColor)

            currentSelection = selected
            binding.contoursToggle.isChecked = currentSelection.showContours
        }

        updateSelection(currentSelection)

        binding.styleOptionOsm.setOnClickListener { updateSelection(MapStyle.OSM(currentSelection.showContours)) }
        binding.styleOptionSatellite.setOnClickListener { updateSelection(MapStyle.SATELLITE(currentSelection.showContours)) }

        binding.contoursToggle.setOnCheckedChangeListener { _, isChecked ->
            currentSelection = when (currentSelection) {
                is MapStyle.OSM -> MapStyle.OSM(isChecked)
                is MapStyle.SATELLITE -> MapStyle.SATELLITE(isChecked)
            }
        }

        MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setNegativeButton("Annulla", null)
            .setPositiveButton("Conferma") { _, _ ->
                if (currentSelection != currentStyle) {
                    onStyleSelected(currentSelection)
                }
            }
            .show()
    }
}