package com.ferrarieugenio.toponomastica_stenico_app.util.exporter

import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym

interface ToponymExporter {
    fun export(toponyms: List<Toponym>): ByteArray
    val fileExtension: String
    val mimeType: String
    val displayName: String
}