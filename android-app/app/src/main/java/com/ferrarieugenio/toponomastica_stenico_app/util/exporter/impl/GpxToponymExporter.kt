package com.ferrarieugenio.toponomastica_stenico_app.util.exporter.impl

import android.util.Xml
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.ToponymExporter
import org.xmlpull.v1.XmlSerializer
import java.io.ByteArrayOutputStream

class GpxToponymExporter : ToponymExporter {

    override fun export(toponyms: List<Toponym>): ByteArray {
        val output = ByteArrayOutputStream()
        val xml: XmlSerializer = Xml.newSerializer()
        xml.setOutput(output, "UTF-8")
        xml.startDocument("UTF-8", true)

        xml.startTag(null, "gpx")
        xml.attribute(null, "version", "1.1")
        xml.attribute(null, "creator", "ToponimiStenicoApp")
        xml.attribute(null, "xmlns", "http://www.topografix.com/GPX/1/1")
        xml.attribute(null, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
        xml.attribute(null, "xsi:schemaLocation",
            "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd"
        )

        for (t in toponyms) {
            xml.startTag(null, "wpt")
            xml.attribute(null, "lat", t.lat.toString())
            xml.attribute(null, "lon", t.lon.toString())

            writeTag(xml, "name", t.nome)
            writeTag(xml, "desc", t.descrizione)
            writeTag(xml, "ele", t.quota.toString())
            writeTag(xml, "cmt", t.forma_ufficiale ?: "")
            writeTag(xml, "sym", "Waypoint")

            // Custom extensions for other fields
            xml.startTag(null, "extensions")

            writeTag(xml, "id", t.id.toString())
            writeTag(xml, "comune", t.comune)
            writeTag(xml, "varianti", t.varianti?.joinToString(", ") ?: "")
            writeTag(xml, "tags", t.tags.joinToString(", "))
            writeTag(xml, "cluster", t.cluster)
            writeTag(xml, "hc_cluster", t.hc_cluster)

            // Neighbors as IDs only
            val neighborIds = t.closest_5_neighbors_ids.joinToString(", ")
            writeTag(xml, "closest_5_neighbors", neighborIds)

            xml.endTag(null, "extensions")

            xml.endTag(null, "wpt")
        }

        xml.endTag(null, "gpx")
        xml.endDocument()

        return output.toByteArray()
    }

    private fun writeTag(xml: XmlSerializer, tag: String, value: String) {
        xml.startTag(null, tag)
        xml.text(value)
        xml.endTag(null, tag)
    }

    override val fileExtension = "gpx"
    override val mimeType = "application/gpx+xml"
    override val displayName = "GPX (GPS Exchange Format)"
}
