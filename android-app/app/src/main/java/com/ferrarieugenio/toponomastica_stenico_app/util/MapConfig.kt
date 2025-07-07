package com.ferrarieugenio.toponomastica_stenico_app.util

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

object MapConfig {
    val DEFAULT_LOCATION = LatLng(46.052168, 10.8540886)
    const val DEFAULT_ZOOM = 14.0

    // todo adjust bounds if needed
    val LOCATION_BOUNDS = LatLngBounds.from(
        46.276000, 11.060000,
        45.978000, 10.655000
    )
    val LOCATION_BOUNDS_WITH_BUFFER = LOCATION_BOUNDS.withBuffer(
        bufferLat = 0.05,
        bufferLon = 0.05
    )
    val MIN_ZOOM_BOUND = 12.0
    val MAX_ZOOM_BOUND = 17.0

    fun LatLngBounds.withBuffer(bufferLat: Double, bufferLon: Double): LatLngBounds {
        val bufferedNorth = this.northEast.latitude - bufferLat
        val bufferedEast = this.northEast.longitude - bufferLon
        val bufferedSouth = this.southWest.latitude + bufferLat
        val bufferedWest = this.southWest.longitude + bufferLon

        return LatLngBounds.from(bufferedNorth, bufferedEast, bufferedSouth, bufferedWest)
    }
}