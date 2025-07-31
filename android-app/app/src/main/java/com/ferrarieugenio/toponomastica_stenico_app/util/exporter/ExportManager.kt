package com.ferrarieugenio.toponomastica_stenico_app.util.exporter

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.documentfile.provider.DocumentFile
import com.ferrarieugenio.toponomastica_stenico_app.util.download.NotificationPermissionHelper

class ExportManager(
    private val context: Context,
    val notificationPermissionHelper: NotificationPermissionHelper
) {

    private var pendingExportData: ByteArray? = null
    private var pendingExportFormat: ExportFormat? = null
    private var pendingExportFileName: String? = null

    fun launchCreateDocument(
        createDocumentLauncher: ActivityResultLauncher<String>
    ) {
        createDocumentLauncher.launch(pendingExportFileName!!)
    }

    fun handleCreateDocumentResult(
        uri: Uri?,
        createSuccessCallback: () -> Unit,
        createFailCallback: () -> Unit
    ) {
        if (uri != null && pendingExportData != null) {
            val success = FileExportUtils.saveTextToUri(context, uri, pendingExportData!!)
            if (success) {
                createSuccessCallback()
                val fileName = DocumentFile.fromSingleUri(context, uri)?.name ?: "file"
                notificationPermissionHelper.checkAndRequestPermission {
                    ExportNotificationHelper.showNotification(
                        context,
                        fileName,
                        uri,
                        pendingExportFormat?.mimeType
                    )
                }
            } else {
                createFailCallback()
            }
        } else {
            createFailCallback()
        }
        pendingExportData = null
        pendingExportFormat = null
    }

    fun prepareExport(data: ByteArray, format: ExportFormat, fileName: String?) {
        pendingExportData = data
        pendingExportFormat = format
        pendingExportFileName = FileExportUtils.getSuggestedFileName(fileName, format)
    }
}
