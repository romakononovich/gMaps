package xyz.romakononovich.gmaps

import android.Manifest
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import android.location.Geocoder
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_maps.*
import permissions.dispatcher.OnPermissionDenied
import java.util.*


@RuntimePermissions
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var marker: Marker
    private lateinit var location: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        loadMapWithPermissionCheck()
    }


    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun loadMap() {
        map.isMyLocationEnabled = true
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (locationManager.getLastKnownLocation(locationManager.getBestProvider(Criteria(), true)) != null) {
            location = locationManager.getLastKnownLocation(locationManager.getBestProvider(Criteria(), true))
            marker = map.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).title(resources.getText(R.string.tv_my_position).toString()))
        } else {
            marker = map.addMarker(MarkerOptions().position(LatLng(1.0, 1.0)).title(resources.getText(R.string.tv_my_position).toString()))
        }

        map.setOnCameraMoveListener {
            marker.position = map.cameraPosition.target
        }
        map.setOnCameraIdleListener {
            val gc = Geocoder(this, Locale.getDefault())
            val addresses = gc.getFromLocation(marker.position.latitude, marker.position.longitude, 1)
            if (!addresses.isEmpty())
                tv.text = addresses.first().getAddressLine(0)
        }
        map.setOnCameraMoveStartedListener {
            tv.text = resources.getText(R.string.tv_search)
        }
        val cameraPosition = CameraPosition.Builder()
                .target(marker.position)
                .zoom(15f)
                .build()
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        map.moveCamera(cameraUpdate)

    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showToast() {
        Toast.makeText(this, resources.getText(R.string.toast_permission), Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }
}
