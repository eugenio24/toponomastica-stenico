package com.ferrarieugenio.toponomastica_stenico_app.util.exporter.impl

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.ToponymExporter
import java.io.ByteArrayOutputStream

class PdfToponymExporter(
    private val toponymRepository: ToponymRepository
) : ToponymExporter {

    override fun export(toponyms: List<Toponym>): ByteArray {
        val document = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        val marginStart = 40
        val maxTextWidth = pageWidth - 2 * marginStart
        val lineSpacing = 22
        val bulletSpacing = 16

        val titlePaint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textSize = 20f
            color = Color.BLACK
        }

        val labelPaint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textSize = 13f
            color = Color.DKGRAY
        }

        val textPaint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.DEFAULT
            textSize = 13f
            color = Color.BLACK
        }

        val bulletPaint = Paint(textPaint)

        for ((index, t) in toponyms.withIndex()) {
            val neighbors = toponymRepository.getToponymNeighbors(t)
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = 60

            // Title
            canvas.drawText(t.nome, marginStart.toFloat(), y.toFloat(), titlePaint)
            y += 30

            fun drawLabelAndValue(label: String, value: String) {
                canvas.drawText("$label:", marginStart.toFloat(), y.toFloat(), labelPaint)
                canvas.drawText(value, (marginStart + 110).toFloat(), y.toFloat(), textPaint)
                y += lineSpacing
            }

            fun drawWrappedText(label: String, value: String) {
                canvas.drawText("$label:", marginStart.toFloat(), y.toFloat(), labelPaint)
                y += lineSpacing
                val lines = wrapText(value, textPaint, maxTextWidth)
                for (line in lines) {
                    canvas.drawText(line, marginStart.toFloat(), y.toFloat(), textPaint)
                    y += lineSpacing
                }
            }

            fun drawBulletList(label: String, items: List<String>) {
                canvas.drawText("$label:", marginStart.toFloat(), y.toFloat(), labelPaint)
                y += lineSpacing
                for (item in items) {
                    canvas.drawText("• $item", (marginStart + bulletSpacing).toFloat(), y.toFloat(), bulletPaint)
                    y += lineSpacing
                }
            }

            drawLabelAndValue("Forma ufficiale", t.forma_ufficiale ?: "Nessuna")
            drawLabelAndValue("Comune", t.comune)
            drawLabelAndValue("Quota", "${t.quota} m")
            drawLabelAndValue("Lat/Lon", "${t.lat}, ${t.lon}")
            drawLabelAndValue("Cluster", t.cluster)
            drawLabelAndValue("HC Cluster", t.hc_cluster)

            drawWrappedText("Descrizione", t.descrizione)
            drawLabelAndValue("Varianti", t.varianti?.joinToString(", ") ?: "Nessuna")
            drawLabelAndValue("Tags", t.tags.joinToString(", "))

            drawBulletList("Vicini più prossimi (5)", neighbors.map { it.nome })

            document.finishPage(page)
        }

        val outputStream = ByteArrayOutputStream()
        document.writeTo(outputStream)
        document.close()
        return outputStream.toByteArray()
    }

    override val fileExtension = "pdf"
    override val mimeType = "application/pdf"
    override val displayName = "PDF"

    private fun wrapText(text: String, paint: Paint, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        for (word in words) {
            val potentialLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(potentialLine) <= maxWidth) {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
}
