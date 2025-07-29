package com.ferrarieugenio.toponomastica_stenico_app.util.exporter.impl

import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import com.ferrarieugenio.toponomastica_stenico_app.util.exporter.ToponymExporter

class TxtToponymExporter(
    private val toponymRepository: ToponymRepository
) : ToponymExporter {

    override fun export(toponyms: List<Toponym>): ByteArray {
        return toponyms.joinToString("\n\n----------------------------------------\n\n") { t ->
            val neighbors = toponymRepository.getToponymNeighbors(t)
            val neighborNames = neighbors.map { it.nome }

            """
            Nome: ${t.nome}
            Forma ufficiale: ${t.forma_ufficiale ?: "Nessuna"}
            Comune: ${t.comune}
            Descrizione: ${t.descrizione}
            Quota: ${t.quota} m
            Lat/Lon: ${t.lat}, ${t.lon}
            Varianti: ${t.varianti?.joinToString(", ") ?: "Nessuna"}
            Tags: ${t.tags.joinToString(", ")}
            Cluster: ${t.cluster}
            HC Cluster: ${t.hc_cluster}
            Vicini più prossimi (5): ${neighborNames.joinToString(", ")}
            """.trimIndent()
        }.toByteArray(Charsets.UTF_8)
    }

    override val fileExtension = "txt"
    override val mimeType = "text/plain"
    override val displayName = "TXT (simple text)"
}