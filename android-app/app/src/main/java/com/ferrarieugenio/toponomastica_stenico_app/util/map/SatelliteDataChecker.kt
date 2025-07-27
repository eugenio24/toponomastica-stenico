package com.ferrarieugenio.toponomastica_stenico_app.util.map

import android.content.Context
import java.io.File

object SatelliteDataChecker {
    private const val SATELLITE_FOLDER = "satellite-stenico"
    private const val METADATA_FILENAME = "satellite-stenico.json"

    fun isSatelliteDataAvailable(context: Context): Boolean {
        val satelliteDir = File(context.filesDir, SATELLITE_FOLDER)
        val metadataFile = File(satelliteDir.parentFile, METADATA_FILENAME)
        return satelliteDir.exists() && satelliteDir.isDirectory && metadataFile.exists()
    }
}