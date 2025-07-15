package com.ferrarieugenio.toponomastica_stenico_app.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class SatelliteDataManager(private val context: Context) {

    companion object {
        private const val TAG = "SatelliteDataManager"
        private const val ZIP_FILENAME = "satellite-data.zip"
        const val SATELLITE_FOLDER = "satellite-stenico"
        private const val METADATA_FILENAME = "satellite-stenico.json"

        private const val REMOTE_ZIP_URL =
            "https://github.com/eugenio24/toponimi-stenico-assets/releases/download/v1.0/satellite-data.zip"
    }

    val satelliteDir = File(context.filesDir, SATELLITE_FOLDER)

    fun isSatelliteDataAvailable(): Boolean {
        val metadataFile = File(satelliteDir.parentFile, METADATA_FILENAME)
        return satelliteDir.exists() && satelliteDir.isDirectory && metadataFile.exists()
    }

    suspend fun downloadAndExtractSatelliteData(onProgress: (percent: Int) -> Unit) {
        withContext(Dispatchers.IO) {
            val zipFile = File(context.cacheDir, ZIP_FILENAME)
            try {
                downloadFile(REMOTE_ZIP_URL, zipFile, onProgress)
                unzip(zipFile, context.filesDir)
                zipFile.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Download/extract failed", e)
                throw e
            }
        }
    }

    private fun downloadFile(urlString: String, destination: File, onProgress: (percent: Int) -> Unit) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()
        val fileLength = connection.contentLength

        connection.inputStream.use { input ->
            FileOutputStream(destination).use { output ->
                val data = ByteArray(4096)
                var total: Long = 0
                var count = input.read(data)
                while (count != -1) {
                    total += count
                    output.write(data, 0, count)
                    val progress = if (fileLength > 0) (total * 100 / fileLength).toInt() else -1
                    if (progress != -1) onProgress(progress)
                    count = input.read(data)
                }
            }
        }
    }

    private fun unzip(zipFile: File, targetDir: File) {
        java.util.zip.ZipInputStream(zipFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val outFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    outFile.outputStream().use { output ->
                        zip.copyTo(output)
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }
}
