package com.example.testmap.network

import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.util.UUID
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object BirdHttpClient {
    private val GUID = UUID.randomUUID()

    private val gson = Gson()
    private val client = OkHttpClient()

    // bird endpoints
    private const val authEndpoint:String = "https://api-auth.prod.birdapp.com/api/v1/auth"
    private const val apiEndpoint:String = "https://api-bird.prod.birdapp.com/bird/nearby"
    private val MEDIA_TYPE:MediaType = "application/json".toMediaType()


    // open-elevation endpoint - used to get elevation data for lat, lng pair
    private const val elevationEndpoint:String = "https://api.open-elevation.com/api/v1/lookup"

    private var refresh:String = ""
    private var access:String = ""
    private var expires = System.currentTimeMillis() / 1000L;

    fun firstAuthPost (email:String) : Boolean {
        println("firstAuthPost called successfully")

        val req = getFirstAuthRequest("/email", email)

        print("firstAuthPost req: $req")
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e:IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, res:Response) {
                res.use {
                    println("firstAuthPost responded $res")
                }
            }
        })

        return true
    }

    fun secondAuthPost (token:String):Boolean {

        val req = getSecondAuthRequest("/magic-link/use", token)
        print("secondAuthPost req: $req")

        client.newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, res: Response) {
                res.use {
                    unpackTokens(res)
                    println(res)
                }
            }
        })

        return true
    }

    fun getNearbyScooters(location: LatLng, radius:Int): BirdsResult? {
        refreshTokens()
        val req = getNearbyRequest(location, radius)
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IOException("Unexpected code $res")
            return gson.fromJson(res.body!!.string(), BirdsResult::class.java)
        }

    }

    private fun getFirstAuthRequest(endpoint:String, email:String): Request {
        val postBody = "{\"email\":\"$email\"}".toRequestBody(MEDIA_TYPE)

        return Request.Builder()
            .url("$authEndpoint$endpoint")
            .header("User-Agent","Bird/4.119.0(co.bird.Ride; build:3; iOS 14.3.0) Alamofire/5.2.2")
            .addHeader("Device-Id", GUID.toString())
            .addHeader("Platform", "ios")
            .addHeader("App-Version", "4.119.0")
            .addHeader("Content-Type", "application/json")
            .post(postBody)
            .build()
    }

    private fun getSecondAuthRequest(endpoint:String, token:String): Request {
        val postBody = "{\"token\":\"$token\"}".toRequestBody(MEDIA_TYPE)

        return Request.Builder()
            .url("$authEndpoint$endpoint")
            .header("User-Agent","Bird/4.119.0(co.bird.Ride; build:3; iOS 14.3.0) Alamofire/5.2.2")
            .addHeader("Device-Id", GUID.toString())
            .addHeader("Platform", "ios")
            .addHeader("App-Version", "4.119.0")
            .addHeader("Content-Type", "application/json")
            .post(postBody)
            .build()
    }

    private fun unpackTokens(res: Response):Boolean {
        val tokens: BirdAuthTokens =
            gson.fromJson(res.body!!.string(), BirdAuthTokens::class.java)

        // unpack tokens
        refresh = tokens.refresh
        access = tokens.access
        expires = (System.currentTimeMillis() / 1000L) + (60 * 60 * 24);
        return true
    }

    private fun getElevation(lat:Double, lng:Double) : Number? {
        val req = Request.Builder()
            .url("$elevationEndpoint?locations=${"%.5f".format(lat)},${"%.5f".format(lng)}")
            .build()

        client.newCall(req).execute().use {res ->
            if (!res.isSuccessful) throw IOException("Unexpected code $res")

            val result:ElevationResult = gson.fromJson(res.body!!.string(), ElevationResult::class.java)

            return result.results[0]["elevation"]
        }

    }

    private fun getNearbyRequest(location:LatLng, radius:Number) : Request {
        val lat = location.latitude
        val lng = location.longitude

        val latStr = "%.5f".format(lat)
        val lngStr = "%.5f".format(lng)

        var altitude:Number? = getElevation(lat, lng)
        if (altitude == null) {
            altitude = 0
        }

        val loc : Map<String, Any> = mapOf(
            "latitude" to latStr,
            "longitude" to lngStr,
            "altitude" to altitude,
            "accuracy" to 65,
            "speed" to -1,
            "heading" to -1
        )

        return Request.Builder()
            .url("$apiEndpoint?latitude=$latStr&longitude=$lngStr&radius=$radius")
            .header("User-Agent","Bird/4.119.0(co.bird.Ride; build:3; iOS 14.3.0) Alamofire/5.2.2")
            .addHeader("Device-id",GUID.toString())
            .addHeader("Platform", "ios")
            .addHeader("App-Version", "4.119.0")
            .addHeader("Content-Type", "application/json")
            .addHeader("legacyrequest", "false")
            .addHeader("Authorization", "Bearer $access")
            .addHeader("Location", gson.toJson(loc) )
            .build()
    }

    private fun refreshTokens() : Boolean  {
        if ((System.currentTimeMillis() / 1000L) > expires) {
            val req = Request.Builder()
                .url("$authEndpoint/refresh/token")
                .header("User-Agent","Bird/4.119.0(co.bird.Ride; build:3; iOS 14.3.0) Alamofire/5.2.2")
                .addHeader("Device-Id",GUID.toString())
                .addHeader("Platform", "ios")
                .addHeader("App-Version", "4.119.0")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $refresh")
                .build()
            client.newCall(req).execute().use { res ->
                if (!res.isSuccessful) throw IOException("Unexpected code $res")
                else return unpackTokens(res)
            }
        }
        return false
    }

}