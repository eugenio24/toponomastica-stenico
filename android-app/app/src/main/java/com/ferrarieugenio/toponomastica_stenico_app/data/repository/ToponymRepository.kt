package com.ferrarieugenio.toponomastica_stenico_app.data.repository

import com.ferrarieugenio.toponomastica_stenico_app.data.datasource.ToponymAssetDataSource
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToponymRepository  @Inject constructor(
    private val dataSource: ToponymAssetDataSource,
    private val filename: String
) {
    private var cachedToponyms: List<Toponym>? = null

    private var availableClusters: List<String> = emptyList()
    private var availableHcClusters: List<String> = emptyList()
    private var availableTags: List<String> = emptyList()

    suspend fun getToponyms(): List<Toponym> {
        if (cachedToponyms == null) {
            cachedToponyms = dataSource.loadToponymsFromAssets(filename)
            extractAvailableFilters()
        }
        return cachedToponyms!!
    }

    fun getToponymNeighbors(toponym: Toponym): List<Toponym> {
        return cachedToponyms!!.filter { it.id in toponym.closest_5_neighbors_ids }
    }

    private fun extractAvailableFilters() {
        val toponyms = cachedToponyms ?: return

        availableClusters = toponyms
            .mapNotNull { it.cluster.takeIf { it.isNotBlank() } }
            .distinct()
            .sorted()

        availableHcClusters = toponyms
            .mapNotNull { it.hc_cluster.takeIf { it.isNotBlank() } }
            .distinct()
            .sorted()

        availableTags = toponyms
            .flatMap { it.tags }
            .distinct()
            .sorted()
    }

    fun getAvailableClusters(): List<String> = availableClusters
    fun getAvailableHcClusters(): List<String> = availableHcClusters
    fun getAvailableTags(): List<String> = availableTags
}