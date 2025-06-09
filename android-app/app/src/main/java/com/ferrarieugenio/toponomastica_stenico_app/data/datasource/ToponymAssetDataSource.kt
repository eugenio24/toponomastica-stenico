package com.ferrarieugenio.toponomastica_stenico_app.data.datasource

import android.content.Context
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ToponymAssetDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun loadToponymsFromAssets(filename: String): List<Toponym> = withContext(Dispatchers.IO) {
        val jsonString = context.assets.open(filename).bufferedReader().use { it.readText() }
        Json.decodeFromString(jsonString)
    }
}