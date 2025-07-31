package com.ferrarieugenio.toponomastica_stenico_app.util.exporter.impl

import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.ToponymExporter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GeoJsonToponymExporter : ToponymExporter {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override fun export(toponyms: List<Toponym>): ByteArray {
        val features = toponyms.map { toponym ->
            GeoJsonFeature(
                geometry = Geometry(
                    coordinates = listOf(toponym.lon, toponym.lat)
                ),
                properties = toponym
            )
        }

        val featureCollection = GeoJsonFeatureCollection(features = features)

        return json.encodeToString(featureCollection).toByteArray(Charsets.UTF_8)
    }

    override fun export(toponym: Toponym): ByteArray {
        val feature = GeoJsonFeature(
            geometry = Geometry(
                coordinates = listOf(toponym.lon, toponym.lat)
            ),
            properties = toponym
        )

        return json.encodeToString(feature).toByteArray(Charsets.UTF_8)
    }

    override val fileExtension = "geojson"
    override val mimeType = "application/geo+json"
    override val displayName = "GeoJSON (GIS)"

    @Serializable
    data class GeoJsonFeatureCollection(
        val type: String = "FeatureCollection",
        val features: List<GeoJsonFeature>
    )

    @Serializable
    data class GeoJsonFeature(
        val type: String = "Feature",
        val geometry: Geometry,
        val properties: Toponym
    )

    @Serializable
    data class Geometry(
        val type: String = "Point",
        val coordinates: List<Double> // [lon, lat]
    )
}