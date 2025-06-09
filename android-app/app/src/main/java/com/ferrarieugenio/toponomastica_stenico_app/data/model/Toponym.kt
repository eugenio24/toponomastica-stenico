package com.ferrarieugenio.toponomastica_stenico_app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Toponym(
    val id: Int,
    val nome: String,
    val forma_ufficiale: String?,
    val comune: String,
    val descrizione: String,
    val quota: Int,
    val varianti: List<String>?,
    val lon: Double,
    val lat: Double,
    val tags: List<String>,
    val cluster: String,
    val hc_cluster: String,
    val closest_5_neighbors_ids: List<Int>
) : Parcelable