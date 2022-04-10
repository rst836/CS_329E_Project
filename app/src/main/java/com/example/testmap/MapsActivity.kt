package com.example.testmap

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.testmap.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val icon = BitmapDescriptorFactory.fromResource(R.drawable.scooter)
        val austin = LatLng(30.2849, -97.7341)
        val austin2 = LatLng(30.2851, -97.7341)
        val austin3 = LatLng(30.2851, -97.7339)
        mMap.addMarker(MarkerOptions().position(austin).title("Marker in Austin").icon(icon))
        mMap.addMarker(MarkerOptions().position(austin2).title("Marker in Austin").icon(icon))
        mMap.addMarker(MarkerOptions().position(austin3).title("Marker in Austin").icon(icon))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(austin, 18f))
        mMap.setOnMarkerClickListener { marker ->
            val fragment = ScooterSelect.newInstance()
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.replace(R.id.map, fragment)
            ft.addToBackStack(null);
            ft.commit()
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.settings_menu,menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.item4 -> {
                val fragment = Feedback.newInstance()
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                ft.replace(R.id.map, fragment)
                ft.addToBackStack(null);
                ft.commit()
                true
            }
            R.id.item2 -> {
                val fragment = History.newInstance()
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                ft.replace(R.id.map, fragment)
                ft.addToBackStack(null);
                ft.commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}