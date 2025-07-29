package com.ferrarieugenio.toponomastica_stenico_app.util.exporter.impl

import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.ToponymExporter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonToponymExporter : ToponymExporter {
    private val json = Json { prettyPrint = true }

    override fun export(toponyms: List<Toponym>): ByteArray {
        return json.encodeToString(toponyms).toByteArray(Charsets.UTF_8)
    }

    override val fileExtension = "json"
    override val mimeType = "application/json"
    override val displayName = "JSON (raw)"
}