package com.ferrarieugenio.toponomastica_stenico_app.util.exporter

enum class ExportFormat(
    val fileExtension: String,
    val mimeType: String,
    val displayName: String
) {
    TXT("txt", "text/plain", "Simple Text"),
    JSON("json", "application/json", "JSON"),
    XML("xml", "application/xml", "XML"),
    GEOJSON("geojson", "application/geo+json", "GeoJSON"),
    GPX("gpx", "application/gpx+xml", "GPX"),
    PDF("pdf", "application/pdf", "PDF")
}