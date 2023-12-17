package net.harutiro.osmdroidtest

import android.widget.TextView
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MapTapController(private val mapView: MapView, private val locationTextView: TextView) {

    init {
        val tapOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p != null) {
                    clearMarkers()
                    placeMarker(p)
                    updateLocationText(p)
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }

            private fun clearMarkers() {
                val overlays = mapView.overlays
                for (i in overlays.size - 1 downTo 0) {
                    val overlay = overlays[i]
                    if (overlay is Marker) {
                        overlays.removeAt(i)
                    }
                }
            }

            private fun placeMarker(point: GeoPoint) {
                val marker = Marker(mapView)
                marker.position = point
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(marker)
                mapView.invalidate()
            }

            private fun updateLocationText(point: GeoPoint) {
                locationTextView.text = "緯度: ${point.latitude} 経度: ${point.longitude}"
            }
        })
        mapView.overlays.add(tapOverlay)
    }
}