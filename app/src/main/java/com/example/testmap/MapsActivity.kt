package com.example.testmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.testmap.PermissionUtils.PermissionDeniedDialog.newInstance
import com.example.testmap.PermissionUtils.isPermissionGranted
import com.example.testmap.api.ClientListener
import com.example.testmap.api.HttpClient
import com.example.testmap.api.birdInterface.BirdScooter
import com.example.testmap.api.limeInterface.LimeScooter
import com.example.testmap.databinding.ActivityMapsBinding
import com.example.testmap.fragmentsManage.ManageAccountFragment
import com.example.testmap.fragmentsManage.TestViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.GsonBuilder
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener,
    ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    private var permissionDenied = false
    lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var limeMarkers = mutableListOf<Marker?>()
    private var birdMarkers = mutableListOf<Marker?>()

    lateinit var viewModel: TestViewModel

    private val gson = GsonBuilder().create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[TestViewModel::class.java]
        viewModel.currBird.value = false
        viewModel.currLime.value = false
        viewModel.nextFrag.value = false
        viewModel.inManage.value = true

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
        val style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night)
        mMap = googleMap
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                mMap.setMapStyle(style)
            }
        }
        enableMyLocation()
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)
        val positionUT = LatLng(30.2862, -97.7394)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(positionUT, 17f))
        mMap.setOnCameraIdleListener(this@MapsActivity)
        mMap.uiSettings.setAllGesturesEnabled(false)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            return
        }

        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            PermissionUtils.RationaleDialog.newInstance(
                LOCATION_PERMISSION_REQUEST_CODE, true
            ).show(supportFragmentManager, "dialog")
            return
        }

        // 3. Otherwise, request permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onMyLocationButtonClick(): Boolean {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.settings_menu,menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.item6 -> {
                if (viewModel.inManage.value == false){
                    val fragment = ManageAccountFragment.newInstance()
                    val fm = supportFragmentManager
                    val ft = fm.beginTransaction()
                    ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    ft.replace(R.id.map, fragment)
                    ft.addToBackStack(null);
                    ft.commit()
                    viewModel.inManage.value = true
                    mMap.uiSettings.setAllGesturesEnabled(false)
                    mMap.setOnMarkerClickListener { true }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    suspend fun updateScootersInView() = coroutineScope {

        launch {
            val birdIcon = BitmapDescriptorFactory.fromResource(R.drawable.bird_pin)
            val limeIcon = BitmapDescriptorFactory.fromResource(R.drawable.lime_pin)

            val cameraPosition:CameraPosition = mMap.cameraPosition
            val visibleRegion:VisibleRegion = mMap.projection.visibleRegion

            HttpClient.subscribe(object: ClientListener {
                override fun onUpdateBirdResults() {
                    // save the login information to the database

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
                        birdMarkers.map { it?.remove() }
                        birdMarkers.clear()
                        for (bird: BirdScooter in birdsList) {
                            val loc = bird.location
                            val location = LatLng(loc["latitude"] as Double, loc["longitude"] as Double)
                            val newMarker = mMap.addMarker(MarkerOptions().position(location).title(bird.code).icon(birdIcon))
                            newMarker.tag = mapOf<String, Any>(
                                "Title" to bird.code,
                                "isLime" to false
                            )
                            birdMarkers.add(newMarker)
                            mMap.setOnMarkerClickListener { marker ->
                                val tagValue = marker.tag as Map<String, Any>
                                val fragment = ScooterSelect.newInstance(tagValue.get("isLime") as Boolean)
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
                        limeMarkers.map { it?.remove() }
                        limeMarkers.clear()
                        for (lime: LimeScooter in limesList) {
                            val attributes = lime.attributes
                            val location = LatLng(attributes["latitude"] as Double, attributes["longitude"] as Double)
                            val newMarker = mMap.addMarker(MarkerOptions().position(location).title(attributes["bike_icon_id"].toString()).icon(limeIcon))
                            newMarker.tag = mapOf<String, Any>(
                                "Title" to attributes["bike_icon_id"].toString(),
                                "isLime" to true
                            )
                            limeMarkers.add(newMarker)
                            mMap.setOnMarkerClickListener { marker ->
                                val tagValue:Map<String, Any> = marker.tag as Map<String, Any>
                                val fragment = ScooterSelect.newInstance(tagValue.get("isLime") as Boolean)
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
                override fun onFailedLimeAccess() {}

            })

            HttpClient.getNearbyScooters(cameraPosition, visibleRegion)
        }
    }

    override fun onCameraIdle() {
        runBlocking { updateScootersInView() }
    }

}