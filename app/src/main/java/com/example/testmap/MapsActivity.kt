package com.example.testmap

import com.example.testmap.network.interfaces.BirdInterface

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.testmap.databinding.ActivityMapsBinding
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import org.json.JSONArray
import kotlin.collections.HashMap
import kotlin.concurrent.thread

import kotlin.math.round

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val GUID = UUID.randomUUID().toString()

    private val birdInterface = BirdInterface.create()
    private var birdsToDisplay = mutableListOf<Map<Any, Any>>()

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
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }


    // the camera stopped moving after some motion by the user
    // send the current location
    fun onCameraMoveCanceled() {
        // When the camera stops moving, add its target to the current path, and draw it on the map.
    }

    private fun getNearbyBirds(lat:Float, long:Float, rad:Int) {
        val latRounded = Math.round(lat * 100000.0) / 100000.0
        val longRounded = Math.round(long * 100000.0) / 100000.0

        val location:Map<String, String> = mapOf("latitude" to latRounded.toString(), "longitude" to longRounded.toString(), "radius" to rad.toString())

        val headers:Map<String, String> = mapOf("location" to JSONObject(location).toString(), "Device-Id" to GUID)

        val nearbyBirds = listOf(1)

        /*
        make the API call and store in val nearbyBirds
         */

        // clear birdsToDisplay
        birdsToDisplay = mutableListOf<Map<Any, Any>>()

        // push all nearbyBirds to birdsToDisplay

        for (i in 0..nearbyBirds.length()) {
            var bird = nearbyBirds.getJSONObject(i).toMap()
            birdsToDisplay.add(bird)
        }

    }

}