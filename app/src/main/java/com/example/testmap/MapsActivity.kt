package com.example.testmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.example.testmap.ManageUIFragments.ManageAccountFragment
import com.example.testmap.Network.BirdHttpClient
import com.example.testmap.Network.BirdListener
import com.example.testmap.Network.BirdScooter

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.testmap.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.SphericalUtil
import org.json.JSONArray
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var markers = mutableListOf<Marker?>()

    private val gson = GsonBuilder().create()

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
        val positionUT = LatLng(30.2862, -97.7394)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(positionUT, 17f))
        mMap.setOnCameraIdleListener(this@MapsActivity)
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true




    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.settings_menu,menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {

            R.id.subitem1 ->{
                val fragment = HowTo.newInstance()
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                ft.replace(R.id.map, fragment)
                ft.addToBackStack(null);
                ft.commit()
                true
            }
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
            R.id.item5 -> {
                val fragment = ContactUs.newInstance()
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                ft.replace(R.id.map, fragment)
                ft.addToBackStack(null);
                ft.commit()
                true
            }
            R.id.item6 -> {
                val fragment = ManageAccountFragment.newInstance()
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

    override fun onCameraIdle() {
        val icon = BitmapDescriptorFactory.fromResource(R.drawable.scooter)

        val loc:LatLng = mMap.cameraPosition.target
        val rad = SphericalUtil.computeDistanceBetween(mMap.projection.visibleRegion.farLeft, loc)

        BirdHttpClient.subscribe(object: BirdListener {
            override fun onUpdateResults() {
                val birds:JSONArray? = BirdHttpClient.results?.getJSONArray("birds")
                val birdsList = mutableListOf<BirdScooter>()
                val jsonLength = birds?.length() as Int
                println("number of scooters found: $jsonLength")
                for (i in 0 until jsonLength) {
                    val obj = birds.getJSONObject(i)
                    birdsList.add(gson.fromJson(obj.toString(), BirdScooter::class.java))
                }
                runOnUiThread {
                    markers.map { it?.remove() }
                    markers.clear()
                    for (bird:BirdScooter in birdsList) {
                        val location = LatLng(bird.location.get("latitude") as Double, bird.location.get("longitude") as Double)
                        val newMarker = mMap.addMarker(MarkerOptions().position(location).title(bird.code).icon(icon))
                        markers.add(newMarker)
                        mMap.setOnMarkerClickListener { marker ->
                            val fragment = ScooterSelect.newInstance()
                            val fm = supportFragmentManager
                            val ft = fm.beginTransaction()
                            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
                            ft.replace(R.id.map, fragment)
                            ft.addToBackStack(null);
                            ft.commit()
                            true
                        }
                    }
                }
            }

            override fun onUpdateAccess() {}
        })
        runBlocking {
            BirdHttpClient.getNearbyScooters(loc, rad)
        }
    }

}