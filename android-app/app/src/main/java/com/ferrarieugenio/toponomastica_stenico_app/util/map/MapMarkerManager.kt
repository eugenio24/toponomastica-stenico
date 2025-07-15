package com.ferrarieugenio.toponomastica_stenico_app.util.map

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import com.ferrarieugenio.toponomastica_stenico_app.R
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.google.gson.JsonObject
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.utils.BitmapUtils


class MapMarkerManager(
    private val context: Context,
    private val mapView: MapView,
    private val map: MapLibreMap,
    private val style: Style,
    private val onMarkerClick: (toponymId: Int) -> Unit
) {
    private val symbolManager: SymbolManager = SymbolManager(mapView, map, style).apply {
        iconAllowOverlap = true
        iconIgnorePlacement = true
        textAllowOverlap = true
        textIgnorePlacement = true
    }

    private val symbolById = mutableMapOf<Int, Symbol>()

    init {
        symbolManager.addClickListener { symbol ->
            symbol.data?.asJsonObject?.get("id")?.asInt?.let { onMarkerClick(it) }
            true
        }
    }

    fun initialize() {
        initIcons(style)
    }

    private fun initIcons(style: Style) {
        listOf(
            MARKER_ICON_NAME_UNSELECTED to R.drawable.ic_marker_unselected,
            MARKER_ICON_NAME_SELECTED to R.drawable.ic_marker_selected
        ).forEach { (name, resId) ->
            ResourcesCompat.getDrawable(context.resources, resId, null)?.let {
                BitmapUtils.getBitmapFromDrawable(it)?.let { bmp ->
                    style.addImage(name, bmp)
                }
            }
        }
    }

    fun addMarkers(toponyms: List<Toponym>, selectedId: Int?, onComplete: (() -> Unit)? = null) {
        symbolManager.deleteAll()
        symbolById.clear()

        toponyms.forEach { toponym ->
            val data = JsonObject().apply { addProperty("id", toponym.id) }
            val symbol = symbolManager.create(
                SymbolOptions()
                    .withLatLng(LatLng(toponym.lat, toponym.lon))
                    .withIconImage(
                        if (toponym.id == selectedId) MARKER_ICON_NAME_SELECTED
                        else MARKER_ICON_NAME_UNSELECTED
                    )
                    .withData(data)
            )
            symbolById[toponym.id] = symbol
        }

        onComplete?.invoke()
    }

    fun updateSelection(prevId: Int?, newId: Int?) {
        prevId?.let { symbolById[it]?.apply { iconImage = MARKER_ICON_NAME_UNSELECTED }?.let { symbolManager.update(it) } }
        newId?.let { symbolById[it]?.apply { iconImage = MARKER_ICON_NAME_SELECTED }?.let { symbolManager.update(it) } }
    }

    fun onDestroy() = symbolManager.onDestroy()

    companion object {
        private const val MARKER_ICON_NAME_UNSELECTED = "marker-pin-unselected"
        private const val MARKER_ICON_NAME_SELECTED = "marker-pin-selected"
    }
}