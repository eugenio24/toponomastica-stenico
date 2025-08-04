package com.ferrarieugenio.toponomastica_stenico_app.util.map

import android.graphics.Bitmap
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions


class MapMarkerManager(
    private val mapView: MapView,
    private val map: MapLibreMap,
    private val style: Style,
    private val iconCache: MarkerIconCache,
    private val namedMarkersZoomThresholds: Double,
    private val onMarkerClick: (toponymId: Int) -> Unit
) {
    private val symbolManager: SymbolManager = SymbolManager(mapView, map, style).apply {
        iconAllowOverlap = true
        iconIgnorePlacement = true
        textAllowOverlap = true
        textIgnorePlacement = true
    }

    private val symbolById = mutableMapOf<Int, Symbol>()
    private val iconIdByToponymIdAndState = mutableMapOf<IconKey, String>()

    private var currentToponyms: List<Toponym> = emptyList()
    private var selectedId: Int? = null
    private var showNamedMarkers = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val loadedStyleImages = mutableSetOf<String>()

    init {
        symbolManager.addClickListener { symbol ->
            symbol.data?.asJsonObject?.get("id")?.asInt?.let { onMarkerClick(it) }
            true
        }

        map.addOnCameraIdleListener {
            val zoom = map.cameraPosition.zoom
            val newShowNamed = zoom >= namedMarkersZoomThresholds
            if (newShowNamed != showNamedMarkers) {
                showNamedMarkers = newShowNamed

                // re-render markers for zoom change
                addMarkers(currentToponyms, selectedId)
                // Re-apply selection to update icons after re-rendering
                setSelectedId(selectedId)
            }
        }
    }

    private fun loadDefaultMarkers() {
        if (style.getImage(MARKER_ICON_NAME_UNSELECTED) == null) {
            iconCache.getDefaultUnselectedBitmap().let { style.addImage(MARKER_ICON_NAME_UNSELECTED, it) }
        }
        if (style.getImage(MARKER_ICON_NAME_SELECTED) == null) {
            iconCache.getDefaultSelectedBitmap().let { style.addImage(MARKER_ICON_NAME_SELECTED, it) }
        }
    }

    suspend fun loadMarkerIcons(toponyms: List<Toponym>) {
        withContext(Dispatchers.Main) {
            loadDefaultMarkers()
        }

        val styleImagesToAdd = mutableListOf<Triple<String, Bitmap, IconKey>>()

        withContext(Dispatchers.IO) {
            for (toponym in toponyms) {
                val id = toponym.id

                val unselected = iconCache.getUnselectedBitmap(id)
                val selected = iconCache.getSelectedBitmap(id)

                unselected?.let {
                    val iconId = "marker_unselected_named_$id"
                    styleImagesToAdd.add(Triple(iconId, it, IconKey(
                        id,
                        isSelected = false,
                        showName = true
                    )))
                }

                selected?.let {
                    val iconId = "marker_selected_named_$id"
                    styleImagesToAdd.add(Triple(iconId, it, IconKey(
                        id,
                        isSelected = true,
                        showName = true
                    )))
                }
            }
        }

        // map interaction should happen in main thread
        withContext(Dispatchers.Main) {
            for ((iconId, bitmap, key) in styleImagesToAdd) {
                addImageIfNotLoaded(iconId, bitmap)
                iconIdByToponymIdAndState[key] = iconId
            }
        }
    }


    fun addMarkers(toponyms: List<Toponym>, selectedId: Int?, onComplete: (() -> Unit)? = null) {
        currentToponyms = toponyms
        this.selectedId = selectedId

        symbolManager.deleteAll()
        symbolById.clear()

        val iconSize = if (showNamedMarkers) NAMED_ICON_SCALE_FACTOR else 1.0f

        toponyms.forEach { toponym ->
            val data = JsonObject().apply {
                addProperty("id", toponym.id)
                addProperty("nome", toponym.nome)
            }

            val isSelected = toponym.id == selectedId
            val fallback = if (isSelected) MARKER_ICON_NAME_SELECTED else MARKER_ICON_NAME_UNSELECTED
            val iconId = iconIdByToponymIdAndState[
                IconKey(toponym.id, isSelected, showNamedMarkers)
            ] ?: fallback

            val symbol = symbolManager.create(
                SymbolOptions()
                    .withLatLng(LatLng(toponym.lat, toponym.lon))
                    .withIconImage(iconId)
                    .withIconAnchor("top")
                    .withIconSize(iconSize)
                    .withData(data)
            )
            symbolById[toponym.id] = symbol
        }

        onComplete?.invoke()
    }

    fun setSelectedId(newSelectedId: Int?) {
        if (newSelectedId == selectedId) return
        val previousId = selectedId
        selectedId = newSelectedId
        updateSelection(previousId, newSelectedId)
    }

    private fun updateSelection(prevId: Int?, newId: Int?) {
        prevId?.let {
            symbolById[it]?.let { symbol ->
                val key = IconKey(it, false, showNamedMarkers)
                val iconId = iconIdByToponymIdAndState[key] ?: MARKER_ICON_NAME_UNSELECTED
                symbol.iconImage = iconId
                symbolManager.update(symbol)
            }
        }
        newId?.let {
            symbolById[it]?.let { symbol ->
                val key = IconKey(it, true, showNamedMarkers)
                val iconId = iconIdByToponymIdAndState[key] ?: MARKER_ICON_NAME_SELECTED
                symbol.iconImage = iconId
                symbolManager.update(symbol)
            }
        }
    }

    private fun addImageIfNotLoaded(iconId: String, bitmap: Bitmap) {
        if (iconId !in loadedStyleImages) {
            style.addImage(iconId, bitmap)
            loadedStyleImages.add(iconId)
        }
    }

    fun onDestroy() {
        symbolManager.onDestroy()
        scope.cancel()
    }

    data class IconKey(val toponymId: Int, val isSelected: Boolean, val showName: Boolean)

    companion object {
        private const val MARKER_ICON_NAME_UNSELECTED = "marker-pin-unselected"
        private const val MARKER_ICON_NAME_SELECTED = "marker-pin-selected"
        private const val NAMED_ICON_SCALE_FACTOR = 1.3f
    }
}
