package com.ferrarieugenio.toponomastica_stenico_app.util.map

sealed class MapStyle(val name: String) {
    data object OSM : MapStyle("OSM")
    data object SATELLITE : MapStyle("SATELLITE")

    companion object {
        fun values(): Array<MapStyle> = arrayOf(OSM, SATELLITE)

        fun valueOf(value: String): MapStyle {
            return when (value) {
                "OSM" -> OSM
                "SATELLITE" -> SATELLITE
                else -> throw IllegalArgumentException("No object MapStyle.$value")
            }
        }
    }
}