package com.ferrarieugenio.toponomastica_stenico_app.util.exporter

import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.impl.GeoJsonToponymExporter
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.impl.GpxToponymExporter
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.impl.JsonToponymExporter
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.impl.PdfToponymExporter
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.impl.TxtToponymExporter
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.impl.XmlToponymExporter

object ExporterFactory {
    fun getExporter(
        format: ExportFormat,
        repository: ToponymRepository? = null
    ): ToponymExporter = when (format) {
        ExportFormat.TXT -> TxtToponymExporter(repository ?: throw IllegalArgumentException("Repository required for TXT export"))
        ExportFormat.JSON -> JsonToponymExporter()
        ExportFormat.GEOJSON -> GeoJsonToponymExporter()
        ExportFormat.GPX -> GpxToponymExporter()
        ExportFormat.XML -> XmlToponymExporter()
        ExportFormat.PDF -> PdfToponymExporter(repository ?: throw IllegalArgumentException("Repository required for PDF export"))
    }
}