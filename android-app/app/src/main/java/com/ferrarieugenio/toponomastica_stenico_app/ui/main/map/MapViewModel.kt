package com.ferrarieugenio.toponomastica_stenico_app.ui.main.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import com.ferrarieugenio.toponomastica_stenico_app.util.map.MapStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: ToponymRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _toponyms = MutableLiveData<List<Toponym>>()
    val toponyms: LiveData<List<Toponym>> = _toponyms

    var previousSelectedId: Int? = null     // needed for UI changes
    private val _selectedToponym = MutableLiveData<Toponym?>(null)
    val selectedToponym: LiveData<Toponym?> = _selectedToponym

    init {
        viewModelScope.launch {
            val list = repository.getToponyms()
            _toponyms.postValue(list)

            // Restore selected toponym and previous id
            val selectedId = savedStateHandle.get<Int>("selectedId")
            val previousId = savedStateHandle.get<Int>("previousSelectedId")
            val matched = list.find { it.id == selectedId }
            previousSelectedId = previousId
            _selectedToponym.value = matched
        }
    }

    fun selectToponymById(id: Int?) {
        previousSelectedId = selectedToponym.value?.id
        savedStateHandle["previousSelectedId"] = previousSelectedId
        savedStateHandle["selectedId"] = id
        val matched = toponyms.value?.find { it.id == id }
        _selectedToponym.value = matched
    }

    fun clearSelectedToponym() {
        previousSelectedId = selectedToponym.value?.id
        savedStateHandle["previousSelectedId"] = previousSelectedId
        savedStateHandle.remove<Int>("selectedId")
        _selectedToponym.value = null
    }

    fun saveMapStyle(style: MapStyle) {
        savedStateHandle["map_style"] = style.name
    }

    fun getSavedMapStyle(): MapStyle {
        val name = savedStateHandle.get<String>("map_style")
        return if (name != null) MapStyle.valueOf(name) else MapStyle.OSM
    }

    fun saveCameraPosition(position: CameraPosition) {
        savedStateHandle["camera_lat"] = position.target?.latitude
        savedStateHandle["camera_lng"] = position.target?.longitude
        savedStateHandle["camera_zoom"] = position.zoom
    }

    fun getSavedCameraPosition(): CameraPosition? {
        val lat = savedStateHandle.get<Double>("camera_lat")
        val lng = savedStateHandle.get<Double>("camera_lng")
        val zoom = savedStateHandle.get<Double>("camera_zoom")
        return if (lat != null && lng != null && zoom != null) {
            CameraPosition.Builder()
                .target(org.maplibre.android.geometry.LatLng(lat, lng))
                .zoom(zoom)
                .build()
        } else null
    }
}
