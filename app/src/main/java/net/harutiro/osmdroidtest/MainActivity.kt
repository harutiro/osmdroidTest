package net.harutiro.osmdroidtest

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider

import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay





class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var map: MapView? = null
    val zoomSize = 17

    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 注意　setContentView　より前に置く
        getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {


            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->

                    Log.d(TAG, location.toString())

                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        val centerPoint = GeoPoint(location.latitude, location.longitude)
                        map = findViewById(R.id.mapView)
                        map?.setTileSource(TileSourceFactory.MAPNIK)
                        val mapController: IMapController? = map?.controller
                        mapController?.setZoom(zoomSize)
                        mapController?.setCenter(centerPoint)

                        // GPS アイコンを表示する
                        val mLocationOverlay = MyLocationNewOverlay(
                            GpsMyLocationProvider(this),
                            map
                        )
                        mLocationOverlay.enableMyLocation()
                        map?.overlays?.add(mLocationOverlay)

                        //コンパスを表示する
                        val compassOverlay = CompassOverlay(this, InternalCompassOrientationProvider(this), map)
                        compassOverlay.enableCompass()
                        map?.overlays?.add(compassOverlay)

                        val tapOverlay = MapEventsOverlay(object: MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                // タップしたらその場所にマーカーを置く
                                // マーカーだけ消去する
                                val overlays = map?.overlays
                                for (i in overlays!!.size - 1 downTo 0) {
                                    val overlay = overlays[i]
                                    if (overlay is Marker) {
                                        overlays.removeAt(i)
                                    }
                                }

                                val marker = Marker(map)
                                marker.position = p
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                map?.overlays?.add(marker)
                                map?.invalidate()

                                // UIスレッドでTextViewを更新する
                                runOnUiThread {
                                    findViewById<TextView>(R.id.location_text).text = "緯度: ${p?.latitude} 経度: ${p?.longitude}"
                                }
                                return true
                            }
                            override fun longPressHelper(p: GeoPoint?): Boolean {
                                // Do whatever
                                return true
                            }
                        })
                        map?.overlays?.add(tapOverlay)
                    }
                }

        }
    }


    override fun onResume() {
        super.onResume()
        map?.onResume()
    }

    override fun onPause() {
        super.onPause()
        map?.onPause()
    }
}