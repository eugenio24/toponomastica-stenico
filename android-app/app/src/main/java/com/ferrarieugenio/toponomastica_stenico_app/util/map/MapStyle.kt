package com.ferrarieugenio.toponomastica_stenico_app.util.map

sealed class MapStyle(
    val name: String,
    open val showContours: Boolean,
    open val showMunicipalities: Boolean = false
) {
    data class OSM(
        override val showContours: Boolean = true,
        override val showMunicipalities: Boolean = true
    ) : MapStyle("OSM", showContours, showMunicipalities)

    data class SATELLITE(
        override val showContours: Boolean = true,
        override val showMunicipalities: Boolean = true
    ) : MapStyle("SATELLITE", showContours, showMunicipalities)

    companion object {
        fun values(): Array<MapStyle> = arrayOf(OSM(), SATELLITE())

        fun valueOf(
            value: String,
            showContours: Boolean,
            showMunicipalities: Boolean = false
        ): MapStyle {
            return when (value) {
                "OSM" -> OSM(showContours, showMunicipalities)
                "SATELLITE" -> SATELLITE(showContours, showMunicipalities)
                else -> throw IllegalArgumentException("No object MapStyle.$value")
            }
        }
    }
}