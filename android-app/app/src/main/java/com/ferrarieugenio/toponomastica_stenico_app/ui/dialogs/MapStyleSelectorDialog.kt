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
                MapStyle.OSM -> binding.styleOptionOsm to binding.textOsm
                MapStyle.SATELLITE -> binding.styleOptionSatellite to binding.textSatellite
            }

            val unselectedViews = when (selected) {
                MapStyle.OSM -> binding.styleOptionSatellite to binding.textSatellite
                MapStyle.SATELLITE -> binding.styleOptionOsm to binding.textOsm
            }

            selectedViews.first.strokeColor = selectedColor
            selectedViews.second.setTextColor(selectedColor)

            unselectedViews.first.strokeColor = unselectedStrokeColor
            unselectedViews.second.setTextColor(unselectedTextColor)

            currentSelection = selected
        }

        updateSelection(currentSelection)

        binding.styleOptionOsm.setOnClickListener { updateSelection(MapStyle.OSM) }
        binding.styleOptionSatellite.setOnClickListener { updateSelection(MapStyle.SATELLITE) }

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