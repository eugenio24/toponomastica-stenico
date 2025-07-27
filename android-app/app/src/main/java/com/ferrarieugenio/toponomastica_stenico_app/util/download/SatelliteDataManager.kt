package com.ferrarieugenio.toponomastica_stenico_app.util.download

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
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

    suspend fun downloadAndExtractSatelliteData(progressChannel: SendChannel<Int>) {
        withContext(Dispatchers.IO) {
            val zipFile = File(context.cacheDir, ZIP_FILENAME)
            try {
                downloadFile(REMOTE_ZIP_URL, zipFile) { percent ->
                    progressChannel.trySend(percent)
                }
                unzip(zipFile, context.filesDir)
                zipFile.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Download/extract failed", e)
                throw e
            } finally {
                progressChannel.close()
            }
        }
    }

    private suspend fun downloadFile(
        urlString: String,
        destination: File,
        maxRetries: Int = 3,
        onProgress: (percent: Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        var attempt = 0
        var lastProgress = -1

        while (attempt < maxRetries && isActive) {
            try {
                // Check if partial file exists and its size for resume
                val downloadedBytes = if (destination.exists()) destination.length() else 0L

                val url = URL(urlString)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000 // 15 seconds
                    readTimeout = 15_000    // 15 seconds
                    // Support resume
                    if (downloadedBytes > 0) {
                        setRequestProperty("Range", "bytes=$downloadedBytes-")
                    }
                    connect()
                }

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw Exception("HTTP error code: $responseCode")
                }

                val contentLength = connection.getHeaderFieldLong("Content-Length", -1)
                val totalLength = if (downloadedBytes > 0 && responseCode == 206) downloadedBytes + contentLength else contentLength

                val inputStream = BufferedInputStream(connection.inputStream)
                val outputStream = if (downloadedBytes > 0) {
                    FileOutputStream(destination, true) // append mode
                } else {
                    FileOutputStream(destination)
                }

                inputStream.use { input ->
                    outputStream.use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalRead = downloadedBytes
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            // Check for coroutine cancellation
                            if (!isActive) {
                                throw Exception("Download cancelled")
                            }
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            if (totalLength > 0) {
                                val progress = (totalRead * 100 / totalLength).toInt()
                                if (progress != lastProgress) {
                                    lastProgress = progress
                                    onProgress(progress)
                                }
                            }
                        }
                        output.flush()
                    }
                }

                // If reached here, success
                return@withContext

            } catch (e: Exception) {
                attempt++
                if (attempt >= maxRetries) {
                    throw e
                }
                // backoff delay before retrying
                delay(2000L * attempt)
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

    fun deleteSatelliteData(): Boolean {
        val metadataFile = File(satelliteDir.parentFile, METADATA_FILENAME)
        val dataDeleted = if (satelliteDir.exists() && satelliteDir.isDirectory) {
            satelliteDir.deleteRecursively()
        } else true

        val metadataDeleted = if (metadataFile.exists()) {
            metadataFile.delete()
        } else true

        return dataDeleted && metadataDeleted
    }
}
