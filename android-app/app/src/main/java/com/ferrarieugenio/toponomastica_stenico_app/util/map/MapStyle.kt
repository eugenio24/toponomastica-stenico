package com.ferrarieugenio.toponomastica_stenico_app.util.map

sealed class MapStyle(val name: String) {
    abstract val showContours: Boolean

    data class OSM(override val showContours: Boolean = true) : MapStyle("OSM")
    data class SATELLITE(override val showContours: Boolean = true) : MapStyle("SATELLITE")

    companion object {
        fun values(): Array<MapStyle> = arrayOf(OSM(), SATELLITE())

        fun valueOf(value: String, showContours: Boolean): MapStyle {
            return when (value) {
                "OSM" -> OSM(showContours)
                "SATELLITE" -> SATELLITE(showContours)
                else -> throw IllegalArgumentException("No object MapStyle.$value")
            }
        }
    }
}
