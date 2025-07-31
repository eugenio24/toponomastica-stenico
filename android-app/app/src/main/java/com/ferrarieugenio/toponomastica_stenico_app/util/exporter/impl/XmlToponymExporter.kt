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
            writeToponym(xml, t)
        }
        xml.endTag(null, "toponyms")

        xml.endDocument()
        return output.toByteArray()
    }

    override fun export(toponym: Toponym): ByteArray {
        val output = ByteArrayOutputStream()
        val xml: XmlSerializer = Xml.newSerializer()
        xml.setOutput(output, "UTF-8")
        xml.startDocument("UTF-8", true)

        writeToponym(xml, toponym)

        xml.endDocument()
        return output.toByteArray()
    }

    private fun writeToponym(xml: XmlSerializer, toponym: Toponym) {
        xml.startTag(null, "toponym")

        writeTag(xml, "id", toponym.id.toString())
        writeTag(xml, "nome", toponym.nome)
        writeTag(xml, "forma_ufficiale", toponym.forma_ufficiale ?: "")
        writeTag(xml, "comune", toponym.comune)
        writeTag(xml, "descrizione", toponym.descrizione)
        writeTag(xml, "quota", toponym.quota.toString())
        writeTag(xml, "lat", toponym.lat.toString())
        writeTag(xml, "lon", toponym.lon.toString())

        writeList(xml, "varianti", "variante", toponym.varianti)
        writeList(xml, "tags", "tag", toponym.tags)
        writeTag(xml, "cluster", toponym.cluster)
        writeTag(xml, "hc_cluster", toponym.hc_cluster)
        writeList(xml, "closest_5_neighbors_ids", "id", toponym.closest_5_neighbors_ids.map { it.toString() })

        xml.endTag(null, "toponym")
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
