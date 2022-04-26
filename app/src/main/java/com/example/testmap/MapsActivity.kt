package com.example.testmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.example.testmap.fragmentsManage.ManageAccountFragment
import com.example.testmap.api.HttpClient
import com.example.testmap.api.ClientListener
import com.example.testmap.api.birdInterface.BirdScooter

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.testmap.databinding.ActivityMapsBinding
import com.example.testmap.api.limeInterface.LimeScooter
import com.google.android.gms.maps.model.*
import org.json.JSONArray
import com.google.gson.GsonBuilder
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
        val fragment = ManageAccountFragment.newInstance()
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out, R.anim.zoom_in, R.anim.zoom_out)
        ft.replace(R.id.map, fragment)
        ft.addToBackStack(null);
        ft.commit()
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
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                ft.replace(R.id.map, fragment)
                ft.addToBackStack(null);
                ft.commit()
                true
            }
            R.id.item2 -> {
                val fragment = History.newInstance()
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                ft.replace(R.id.map, fragment)
                ft.addToBackStack(null);
                ft.commit()
                true
            }
            R.id.item5 -> {
                val fragment = ContactUs.newInstance()
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                ft.replace(R.id.map, fragment)
                ft.addToBackStack(null);
                ft.commit()
                true
            }
            R.id.item6 -> {
                val fragment = ManageAccountFragment.newInstance()
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                ft.replace(R.id.map, fragment)
                ft.addToBackStack(null);
                ft.commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCameraIdle() {
        val birdIcon = BitmapDescriptorFactory.fromResource(R.drawable.bird_pin)
        val limeIcon = BitmapDescriptorFactory.fromResource(R.drawable.lime_pin)

        val cameraPosition:CameraPosition = mMap.cameraPosition
        val visibleRegion:VisibleRegion = mMap.projection.visibleRegion

        markers.map { it?.remove() }
        markers.clear()

        HttpClient.subscribe(object: ClientListener {
            override fun onUpdateBirdResults() {
                val birds:JSONArray? = HttpClient.birdResults?.getJSONArray("birds")

                if (birds?.length() == 0) {
                    return
                }
                val birdsList = mutableListOf<BirdScooter>()
                println("number of scooters found: ${birds!!.length()}")
                for (i in 0 until birds!!.length() as Int) {
                    val obj = birds.getJSONObject(i)
                    birdsList.add(gson.fromJson(obj.toString(), BirdScooter::class.java))
                }
                runOnUiThread {
                    for (bird: BirdScooter in birdsList) {
                        val loc = bird.location
                        val location = LatLng(loc["latitude"] as Double, loc["longitude"] as Double)
                        val newMarker = mMap.addMarker(MarkerOptions().position(location).title(bird.code).icon(birdIcon))
                        markers.add(newMarker)
                        mMap.setOnMarkerClickListener { marker ->
                            val fragment = ScooterSelect.newInstance()
                            val fm = supportFragmentManager
                            val ft = fm.beginTransaction()
                            ft.setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out, R.anim.zoom_in, R.anim.zoom_out)
                            ft.replace(R.id.map, fragment)
                            ft.addToBackStack(null);
                            ft.commit()
                            true
                        }
                    }
                }
            }

            override fun onUpdateLimeResults() {
                val attributes:JSONObject? = HttpClient.limeResults?.getJSONObject("attributes")
                val limes:JSONArray? = attributes?.getJSONArray("bikes")
                val limesList = mutableListOf<LimeScooter>()
                println("number of scooters found: ${limes?.length()}")
                if (limes?.length() == 0) {
                    return
                }
                for (i in 0 until limes?.length() as Int) {
                    val obj = limes.getJSONObject(i)
                    limesList.add(gson.fromJson(obj.toString(), LimeScooter::class.java))
                }
                runOnUiThread {
                    for (lime: LimeScooter in limesList) {
                        val attributes = lime.attributes
                        val location = LatLng(attributes["latitude"] as Double, attributes["longitude"] as Double)
                        val newMarker = mMap.addMarker(MarkerOptions().position(location).title(attributes["bike_icon_id"].toString()).icon(limeIcon))
                        markers.add(newMarker)
                        mMap.setOnMarkerClickListener { marker ->
                            val fragment = ScooterSelect.newInstance()
                            val fm = supportFragmentManager
                            val ft = fm.beginTransaction()
                            ft.setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out, R.anim.zoom_in, R.anim.zoom_out)
                            ft.replace(R.id.map, fragment)
                            ft.addToBackStack(null);
                            ft.commit()
                            true
                        }
                    }
                }
            }


            override fun onUpdateBirdAccess() {}
            override fun onUpdateLimeAccess() {}
            override fun onFailedBirdAccess() {}

            override fun onFailedLimeAccess() {
                TODO("Not yet implemented")
            }

        })
        runBlocking {
            HttpClient.getNearbyScooters(cameraPosition, visibleRegion)
        }
    }

}