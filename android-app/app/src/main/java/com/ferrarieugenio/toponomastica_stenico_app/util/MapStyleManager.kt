package com.ferrarieugenio.toponomastica_stenico_app.util

import android.content.Context
import android.net.Uri
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet
import java.io.File

class MapStyleManager(private val context: Context) {

    sealed class StyleSetupResult {
        data class Success(val styleFile: File? = null, val styleBuilder: Style.Builder? = null) : StyleSetupResult()
        data class Error(val exception: Exception) : StyleSetupResult()
    }

    fun setupStyle(style: MapStyle): StyleSetupResult {
        return try {
            when (style) {
                MapStyle.OSM -> setupOsmStyle()
                MapStyle.SATELLITE -> setupSatelliteStyle()
            }
        } catch (e: Exception) {
            StyleSetupResult.Error(e)
        }
    }

    fun setupOsmStyle(): StyleSetupResult {
        val styleFile = copyAssetToInternal(STYLE_FILENAME)
        val mbtilesFile = copyAssetToInternal(MBTILES_FILENAME)
        val mbtilesFileContours = copyAssetToInternal(CONTOUR_MBTILES_FILENAME)

        updateStyleFileUri(styleFile, mbtilesFile, mbtilesFileContours)

        val styleBuilder = Style.Builder().fromUri(Uri.fromFile(styleFile).toString())

        return StyleSetupResult.Success(styleBuilder = styleBuilder)
    }

    private fun setupSatelliteStyle(): StyleSetupResult {
        val satelliteManager = SatelliteDataManager(context)

        if (!satelliteManager.isSatelliteDataAvailable()) {
            throw IllegalStateException("Satellite data not downloaded yet")
        }

        val satelliteDir = File(context.filesDir, SatelliteDataManager.SATELLITE_FOLDER)

        val rasterSource = RasterSource(
            "offline-raster-source",
            TileSet("tileset", "file://${satelliteDir.absolutePath}/{z}/{x}/{y}.jpg"),
            256
        )
        val rasterLayer = RasterLayer("offline-raster-layer", "offline-raster-source")

        val styleBuilder = Style.Builder()
            .fromUri("asset://satellite-style.json")
            .withSource(rasterSource)
            .withLayer(rasterLayer)

        return StyleSetupResult.Success(styleBuilder = styleBuilder)
    }

    private fun copyAssetToInternal(assetFileName: String): File {
        context.assets.open(assetFileName).use { input ->
            val outputFile = File(context.filesDir, assetFileName)
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
            return outputFile
        }
    }

    private fun updateStyleFileUri(styleFile: File, mbtilesFile: File, mbtilesFileContours: File) {
        val styleContent = styleFile.readText()
        val updatedContent = styleContent.replace(
            FILE_URI_PLACEHOLDER,
            "mbtiles:///${mbtilesFile.absolutePath}"
        ).replace(
            CONTOUR_FILE_URI_PLACEHOLDER,
            "mbtiles:///${mbtilesFileContours.absolutePath}"
        )
        styleFile.writeText(updatedContent)
    }

    companion object {
        private const val STYLE_FILENAME = "osm-style.json"
        private const val MBTILES_FILENAME = "stenico-osm.mbtiles"
        private const val FILE_URI_PLACEHOLDER = "___FILE_URI___"
        private const val CONTOUR_MBTILES_FILENAME = "stenico-contours.mbtiles"
        private const val CONTOUR_FILE_URI_PLACEHOLDER = "___CONTOURS_FILE_URI___"
    }
}
