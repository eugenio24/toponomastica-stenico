package com.ferrarieugenio.toponomastica_stenico_app.util.map

import android.content.Context
import android.net.Uri
import com.ferrarieugenio.toponomastica_stenico_app.util.download.SatelliteDataManager
import org.maplibre.android.maps.Style
import java.io.File

class MapStyleManager(private val context: Context) {

    sealed class StyleSetupResult {
        data class Success(val styleFile: File? = null, val styleBuilder: Style.Builder? = null) : StyleSetupResult()
        data class Error(val exception: Exception) : StyleSetupResult()
    }

    fun setupStyle(style: MapStyle): StyleSetupResult {
        return try {
            when (style) {
                is MapStyle.OSM -> setupOsmStyle()
                is MapStyle.SATELLITE -> setupSatelliteStyle()
            }
        } catch (e: Exception) {
            StyleSetupResult.Error(e)
        }
    }

    private fun setupOsmStyle(): StyleSetupResult {
        val styleFile = copyAssetToInternal(OSM_STYLE_FILENAME)
        val mbtilesFile = copyAssetToInternal(MBTILES_FILENAME)
        val mbtilesFileContours = copyAssetToInternal(CONTOUR_MBTILES_FILENAME)

        var styleContent = styleFile.readText()

        styleContent = styleContent.replace(
            FILE_URI_PLACEHOLDER,
            "mbtiles:///${mbtilesFile.absolutePath}"
        ).replace(
            CONTOUR_FILE_URI_PLACEHOLDER,
            "mbtiles:///${mbtilesFileContours.absolutePath}"
        )

        styleFile.writeText(styleContent)

        val builder = Style.Builder()
            .fromUri(Uri.fromFile(styleFile).toString())

        return StyleSetupResult.Success(styleFile = styleFile, styleBuilder = builder)
    }

    private fun setupSatelliteStyle(): StyleSetupResult {
        if (!SatelliteDataChecker.isSatelliteDataAvailable(context)) {
            throw IllegalStateException("Satellite data not downloaded yet")
        }

        val styleFile = copyAssetToInternal(SATELLITE_STYLE_FILENAME)

        val mbtilesFileContours = copyAssetToInternal(CONTOUR_MBTILES_FILENAME)
        val satelliteDir = File(context.filesDir, SatelliteDataManager.SATELLITE_FOLDER)

        var styleContent = styleFile.readText()

        styleContent = styleContent.replace(
            SATELLITE_FOLDER_URI_PLACEHOLDER,
            "file://${satelliteDir.absolutePath}/{z}/{x}/{y}.jpg"
        ).replace(
            CONTOUR_FILE_URI_PLACEHOLDER,
            "mbtiles:///${mbtilesFileContours.absolutePath}"
        )

        styleFile.writeText(styleContent)

        val builder = Style.Builder()
            .fromUri(Uri.fromFile(styleFile).toString())

        return StyleSetupResult.Success(styleFile = styleFile, styleBuilder = builder)
    }

    private fun copyAssetToInternal(assetFileName: String): File {
        val outputFile = File(context.filesDir, assetFileName)
        if (outputFile.exists()) return outputFile

        context.assets.open(assetFileName).use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return outputFile
    }

    companion object {
        private const val OSM_STYLE_FILENAME = "osm-style.json"
        private const val SATELLITE_STYLE_FILENAME = "satellite-style.json"

        private const val MBTILES_FILENAME = "stenico-osm.mbtiles"
        private const val FILE_URI_PLACEHOLDER = "___FILE_URI___"

        private const val CONTOUR_MBTILES_FILENAME = "stenico-contours.mbtiles"
        private const val CONTOUR_FILE_URI_PLACEHOLDER = "___CONTOURS_FILE_URI___"

        private const val SATELLITE_FOLDER_URI_PLACEHOLDER = "___SATELLITE_FOLDER_URI___"
    }
}
