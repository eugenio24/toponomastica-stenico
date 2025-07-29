package com.ferrarieugenio.toponomastica_stenico_app.util.exporter

import android.content.Context
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileExportUtils {
    fun saveTextToUri(context: Context, uri: Uri, content: ByteArray): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(content)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getSuggestedFileName(baseName: String?, format: ExportFormat): String {
        return if (!baseName.isNullOrBlank()) {
            "${baseName.trim()}.${format.fileExtension}"
        } else {
            val dateSuffix = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            "toponym_export_$dateSuffix.${format.fileExtension}"
        }
    }
}