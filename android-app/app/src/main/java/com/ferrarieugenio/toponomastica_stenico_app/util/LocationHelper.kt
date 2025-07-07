package com.ferrarieugenio.toponomastica_stenico_app.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap

class LocationHelper(
    private val context: Context,
    private val map: MapLibreMap,
    private val requestPermissionLauncher: ActivityResultLauncher<String>
) {

    fun checkAndEnableLocation(onPermissionDenied: () -> Unit = {}, onLocationAvailable: () -> Unit = {}) {
        if (hasLocationPermission()) {
            enableLocationComponent(onLocationAvailable)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            onPermissionDenied()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(onLocationAvailable: () -> Unit) {
        val style = map.style ?: return
        val locationComponent = map.locationComponent

        val activationOptions = LocationComponentActivationOptions.builder(context, style)
            .useDefaultLocationEngine(true)
            .build()

        locationComponent.activateLocationComponent(activationOptions)
        locationComponent.isLocationComponentEnabled = true
        locationComponent.cameraMode = CameraMode.NONE
        locationComponent.renderMode = RenderMode.COMPASS

        val lastLocation = locationComponent.lastKnownLocation
        if (lastLocation != null) {
            val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0))
            onLocationAvailable()
        } else {
            Toast.makeText(context, "Unable to access your location", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    fun disableLocationComponent() {
        val locationComponent = map.locationComponent
        locationComponent.isLocationComponentEnabled = false
    }

    fun zoomToUserLocation() {
        map.locationComponent.lastKnownLocation?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0))
        }
    }
}
