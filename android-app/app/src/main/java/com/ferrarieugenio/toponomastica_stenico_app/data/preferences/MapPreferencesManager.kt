package com.ferrarieugenio.toponomastica_stenico_app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ferrarieugenio.toponomastica_stenico_app.di.mapPreferencesDataStore
import com.ferrarieugenio.toponomastica_stenico_app.util.map.MapConfig
import com.ferrarieugenio.toponomastica_stenico_app.util.map.MapStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.mapPreferencesDataStore

    private object Keys {
        val MAP_STYLE = stringPreferencesKey("map_style")
        val SHOW_CONTOURS = booleanPreferencesKey("show_contours")
        val SHOW_MUNICIPALITIES = booleanPreferencesKey("show_municipalities")
        val ZOOM_LEVEL_MARKERS = doublePreferencesKey("zoom_level_named_markers")
    }

    val mapStyle: Flow<MapStyle> = dataStore.data.map { prefs ->
        val styleName = prefs[Keys.MAP_STYLE] ?: "OSM"
        val showContours = prefs[Keys.SHOW_CONTOURS] ?: true
        val showMunicipalities = prefs[Keys.SHOW_MUNICIPALITIES] ?: true

        MapStyle.valueOf(styleName, showContours, showMunicipalities)
    }

    val zoomLevelForNamedMarkers: Flow<Double> = dataStore.data.map { prefs ->
        prefs[Keys.ZOOM_LEVEL_MARKERS] ?: MapConfig.DEFAULT_NAMED_MARKER_ZOOM_THRESHOLD
    }

    suspend fun setMapStyle(style: MapStyle) {
        dataStore.edit { prefs ->
            prefs[Keys.MAP_STYLE] = style.name
            prefs[Keys.SHOW_CONTOURS] = style.showContours
            prefs[Keys.SHOW_MUNICIPALITIES] = style.showMunicipalities
        }
    }

    suspend fun setZoomLevelForNamedMarkers(zoom: Double) {
        dataStore.edit { prefs ->
            prefs[Keys.ZOOM_LEVEL_MARKERS] = zoom
        }
    }
}
