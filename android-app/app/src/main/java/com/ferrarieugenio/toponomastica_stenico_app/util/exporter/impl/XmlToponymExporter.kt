package com.ferrarieugenio.toponomastica_stenico_app.util.exporter.impl

import android.util.Xml
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.ToponymExporter
import org.xmlpull.v1.XmlSerializer
import java.io.ByteArrayOutputStream

class XmlToponymExporter : ToponymExporter {

    override fun export(toponyms: List<Toponym>): ByteArray {
        val output = ByteArrayOutputStream()
        val xml: XmlSerializer = Xml.newSerializer()
        xml.setOutput(output, "UTF-8")
        xml.startDocument("UTF-8", true)

        xml.startTag(null, "toponyms")

        for (t in toponyms) {
            xml.startTag(null, "toponym")

            writeTag(xml, "id", t.id.toString())
            writeTag(xml, "nome", t.nome)
            writeTag(xml, "forma_ufficiale", t.forma_ufficiale ?: "")
            writeTag(xml, "comune", t.comune)
            writeTag(xml, "descrizione", t.descrizione)
            writeTag(xml, "quota", t.quota.toString())
            writeTag(xml, "lat", t.lat.toString())
            writeTag(xml, "lon", t.lon.toString())

            writeList(xml, "varianti", "variante", t.varianti)
            writeList(xml, "tags", "tag", t.tags)
            writeTag(xml, "cluster", t.cluster)
            writeTag(xml, "hc_cluster", t.hc_cluster)
            writeList(xml, "closest_5_neighbors_ids", "id", t.closest_5_neighbors_ids.map { it.toString() })

            xml.endTag(null, "toponym")
        }

        xml.endTag(null, "toponyms")
        xml.endDocument()
        return output.toByteArray()
    }

    private fun writeTag(xml: XmlSerializer, tag: String, value: String) {
        xml.startTag(null, tag)
        xml.text(value)
        xml.endTag(null, tag)
    }

    private fun writeList(xml: XmlSerializer, containerTag: String, itemTag: String, values: List<String>?) {
        xml.startTag(null, containerTag)
        values?.forEach { writeTag(xml, itemTag, it) }
        xml.endTag(null, containerTag)
    }

    override val fileExtension = "xml"
    override val mimeType = "application/xml"
    override val displayName = "XML (full data)"
}