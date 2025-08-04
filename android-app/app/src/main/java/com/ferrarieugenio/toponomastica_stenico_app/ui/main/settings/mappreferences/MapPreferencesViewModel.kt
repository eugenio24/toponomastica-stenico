package com.ferrarieugenio.toponomastica_stenico_app.ui.main.settings.mappreferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferrarieugenio.toponomastica_stenico_app.data.preferences.MapPreferencesManager
import com.ferrarieugenio.toponomastica_stenico_app.util.map.MapConfig
import com.ferrarieugenio.toponomastica_stenico_app.util.map.MapStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapPreferencesViewModel @Inject constructor(
    private val preferencesManager: MapPreferencesManager
) : ViewModel() {

    val uiState: StateFlow<MapPreferencesUiState> = combine(
        preferencesManager.mapStyle,
        preferencesManager.zoomLevelForNamedMarkers
    ) { mapStyle, zoomLevel ->
        MapPreferencesUiState(
            mapStyle = mapStyle,
            zoomLevel = zoomLevel,
            isLoaded = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = MapPreferencesUiState.Loading
    )

    fun setMapStyle(styleName: String) = viewModelScope.launch {
        val current = preferencesManager.mapStyle.first()
        val updated = MapStyle.valueOf(styleName, current.showContours, current.showMunicipalities)
        preferencesManager.setMapStyle(updated)
    }

    fun setShowContours(show: Boolean) = viewModelScope.launch {
        val current = preferencesManager.mapStyle.first()
        val updated = MapStyle.valueOf(current.name, show, current.showMunicipalities)
        preferencesManager.setMapStyle(updated)
    }

    fun setShowMunicipalities(show: Boolean) = viewModelScope.launch {
        val current = preferencesManager.mapStyle.first()
        val updated = MapStyle.valueOf(current.name, current.showContours, show)
        preferencesManager.setMapStyle(updated)
    }

    fun setZoomLevelForNamedMarkers(zoom: Double) = viewModelScope.launch {
        preferencesManager.setZoomLevelForNamedMarkers(zoom)
    }
}

data class MapPreferencesUiState(
    val mapStyle: MapStyle = MapStyle.default(),
    val zoomLevel: Double = MapConfig.DEFAULT_NAMED_MARKER_ZOOM_THRESHOLD,
    val isLoaded: Boolean = false
) {
    companion object {
        val Loading = MapPreferencesUiState(isLoaded = false)
    }
}
