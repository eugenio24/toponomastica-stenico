package com.ferrarieugenio.toponomastica_stenico_app.data.repository

import com.ferrarieugenio.toponomastica_stenico_app.data.datasource.ToponymAssetDataSource
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToponymRepository  @Inject constructor(
    private val dataSource: ToponymAssetDataSource,
    private val filename: String = "toponimi.json"
) {

    private var cachedToponyms: List<Toponym>? = null

    suspend fun getToponyms(): List<Toponym> {
        if (cachedToponyms == null) {
            cachedToponyms = dataSource.loadToponymsFromAssets(filename)
        }
        return cachedToponyms!!
    }

}