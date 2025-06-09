package com.ferrarieugenio.toponomastica_stenico_app.ui.components


import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import org.mapsforge.core.graphics.Bitmap
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.Point
import org.mapsforge.map.layer.overlay.Marker

class ToponymMarker(
    latLong: LatLong?,
    bitmap: Bitmap?,
    horizontalOffset: Int,
    verticalOffset: Int,
    private var toponym: Toponym
) :
    Marker(latLong, bitmap, horizontalOffset, verticalOffset) {

    private var action: Runnable? = null

    fun setOnTabAction(action: Runnable?) {
        this.action = action
    }

    override fun onTap(
        tapLatLong: LatLong,
        layerXY: Point,
        tapXY: Point
    ): Boolean {
        val centerX: Double = layerXY.x + horizontalOffset
        val centerY: Double = layerXY.y + verticalOffset

        val radiusX = (bitmap.width / 2) * 1.1
        val radiusY = (bitmap.height / 2) * 1.1

        val distX: Double = Math.abs(centerX - tapXY.x)
        val distY: Double = Math.abs(centerY - tapXY.y)


        if (distX < radiusX && distY < radiusY) {
            if (action != null) {
                action!!.run()
                return true
            }
        }
        return false
    }
}