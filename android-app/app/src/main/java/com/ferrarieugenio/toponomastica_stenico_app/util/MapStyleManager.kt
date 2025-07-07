package com.ferrarieugenio.toponomastica_stenico_app.util

import android.content.Context
import java.io.File

class MapStyleManager(private val context: Context) {
    sealed class StyleSetupResult {
        data class Success(val styleFile: File) : StyleSetupResult()
        data class Error(val exception: Exception) : StyleSetupResult()
    }

    fun setupStyle(): StyleSetupResult {
        return try {
            val styleFile = copyAssetToInternal(STYLE_FILENAME)
            val mbtilesFile = copyAssetToInternal(MBTILES_FILENAME)
            val mbtilesFileContours = copyAssetToInternal(CONTOUR_MBTILES_FILENAME)

            updateStyleFileUri(styleFile, mbtilesFile, mbtilesFileContours)
            StyleSetupResult.Success(styleFile)
        } catch (e: Exception) {
            StyleSetupResult.Error(e)
        }
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
            "mbtiles:///${mbtilesFileContours.absolutePath}")
        styleFile.writeText(updatedContent)
    }

    companion object {
        private const val STYLE_FILENAME = "style.json"

        private const val MBTILES_FILENAME = "stenico-osm.mbtiles"
        private const val FILE_URI_PLACEHOLDER = "___FILE_URI___"

        private const val CONTOUR_MBTILES_FILENAME = "stenico-contours.mbtiles"
        private const val CONTOUR_FILE_URI_PLACEHOLDER = "___CONTOURS_FILE_URI___"
    }
}