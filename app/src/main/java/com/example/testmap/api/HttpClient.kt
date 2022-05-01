package com.example.testmap.api

import com.example.testmap.api.birdInterface.AuthTokens
import com.example.testmap.api.limeInterface.AuthToken
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.VisibleRegion
import java.io.IOException
import java.util.UUID
import com.google.gson.Gson
import com.google.maps.android.SphericalUtil
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.properties.Delegates


object HttpClient {
    private val GUID = UUID.randomUUID()

    private val gson = Gson()
    private val clientBird = OkHttpClient()

    private val clientLime:OkHttpClient

    init {
        // building the okhttpclient with the cookiejar interface implementation
        val builder = OkHttpClient.Builder()
        builder.cookieJar(object : CookieJar {

            private var cookieStore:List<Cookie> = listOf<Cookie>()

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore
            }

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore = cookies
            }

        })
        clientLime = builder.build()
    }

    // bird endpoints
    private const val BIRD_AUTH_ENDPOINT:String = "https://api-auth.prod.birdapp.com/api/v1/auth"
    private const val BIRD_API_ENDPOINT:String = "https://api-bird.prod.birdapp.com/bird/nearby"

    // lime endpoints
    private const val LIME_BASE_URL:String = "https://web-production.lime.bike/api/rider"
    private const val LIME_AUTH_ENDPOINT:String = "${LIME_BASE_URL}/v1/login"
    private const val LIME_API_ENDPOINT:String = "${LIME_BASE_URL}/v1/views/map"

    // media types
    private val JSON_MEDIA_TYPE:MediaType = "application/json".toMediaType()

    // observers list
    private var observers = mutableListOf<ClientListener>()

    private val access =  object {
        val Bird = object {
            var refresh:String by Delegates.observable("") {
                    prop, old, new ->
                println("Updated Bird refresh token $old -> $new")
            }
            var access:String by Delegates.observable("") {
                    prop, old, new ->
                println("Updated Bird access token $old -> $new")
                observers.map { it.onUpdateBirdAccess() }
            }
        }

        val Lime = object {
            var phone:String? = null
            var email:String? = null
            var methodLastUsed:String? = null

            var token:String by Delegates.observable("") {
                    prop, old, new ->
                println("Updated Bird access token $old -> $new")
                observers.map { it.onUpdateLimeAccess() }
            }

            fun setIdInfo(idInfo: String, method:String) {
                if (method == "phone" || method == "email") {
                    this.methodLastUsed = method
                    if (method == "phone") {
                        this.phone = idInfo
                    } else if (method == "email") {
                        this.email = idInfo
                    }
                }
            }
        }
    }

    private var expires:Long by Delegates.observable(System.currentTimeMillis() / 1000L) {
        prop, old, new ->
        println("Updated expiration $old -> $new")

    }

    // public - so that the mapactivity can listen to updates
    var birdResults:JSONObject? by Delegates.observable(null) {
            prop, old, new ->
        println("Updated Bird scooter results $old -> $new")
        observers.map {
                observer ->
            observer.onUpdateBirdResults()
        }
    }
    var limeResults:JSONObject? by Delegates.observable(null) {
            prop, old, new ->
        println("Updated Lime scooter results $old -> $new")
        observers.map {
                observer ->
            observer.onUpdateLimeResults()
        }
    }


    fun subscribe(clientListener: ClientListener) {
        observers.add(clientListener)
    }

    fun unsubscribe(clientListener: ClientListener) {
        observers.remove(clientListener)
    }

    fun loginBird1 (email:String) : Boolean {
        println("loginBird1 called successfully")

        val req = getBirdLogin1Req("/email", email)

        print("loginBird1 req: $req")
        clientBird.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e:IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response:Response) {
                response.use {
                    println("loginBird1 responded $response")
                    if (!response.isSuccessful) {
                        observers.map{it.onFailedBirdAccess()}
                    }
                }
            }
        })

        return true
    }

    fun loginBird2 (token:String):Boolean {
        val req = getBirdLogin2Req("/magic-link/use", token)
        print("loginBird2 req: $req")

        clientBird.newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                observers.map {it.onFailedBirdAccess()}

            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    println("loginBird2 responded")
                    try {
                        unpackBirdTokens(response)
                    } catch (e:Throwable) {
                        e.printStackTrace()
                        observers.map {it.onFailedBirdAccess()}
                    }
                }
            }
        })

        return true
    }

    fun loginLime1 (idInfo:String, callType:String="email") : Boolean {
        println("loginPost called successfully")

        access.Lime.setIdInfo(idInfo, callType)
        val req = getLimeLogin1Req()

        print("loginPost req: $req")
        clientBird.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e:IOException) {
                observers.map {it.onFailedLimeAccess()}
            }

            override fun onResponse(call: Call, response:Response) {
                response.use {
                    println("lime response")
                    println("$response")
                    if (!response.isSuccessful) {
                        observers.map{it.onFailedLimeAccess()}
                    }
                }
            }
        })

        return true
    }

    fun loginLime2 (token:String):Boolean {
        val req = getLimeLogin2Req(token)
        print("loginLime2 req: $req")

        clientLime.newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                observers.map {it.onFailedLimeAccess()}

            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    println("loginLime2 responded")
                    try {
                        unpackLimeTokens(response)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        observers.map {it.onFailedLimeAccess()}
                    }
                }
            }
        })

        return true
    }

    fun getNearbyScooters(cameraPosition:CameraPosition, visibleRegion: VisibleRegion) {
        // if valid access to bird scooters
        if (access.Bird.access != "") {
            val target = cameraPosition.target
            val radius = SphericalUtil.computeDistanceBetween(target, visibleRegion.farLeft)

            val req = getNearbyBirdReq(target, radius)

            clientBird.newCall(req).enqueue(object:Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }
                override fun onResponse(call:Call, response:Response) {
                    response.use {
                        val bodyString:String = response.body!!.string()
                        println(bodyString)
                        birdResults = JSONObject(bodyString)
                    }
                }
            })

        }
        // if valid access to lime scooters
        if (access.Lime.token != "") {
            val req = getNearbyLimeReq(cameraPosition, visibleRegion)
            clientBird.newCall(req).enqueue(object:Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }
                override fun onResponse(call:Call, response:Response) {
                    response.use {
                        val bodyString:String = response.body!!.string()
                        println(bodyString)
                        limeResults = JSONObject(bodyString).getJSONObject("data")
                    }
                }
            })

        }
    }

    private fun getNearbyLimeReq(cameraPosition: CameraPosition, visibleRegion: VisibleRegion) : Request {
        val locationNE:LatLng
        val locationSW:LatLng

        val bearing = cameraPosition.bearing
        val target = cameraPosition.target


        if (bearing < 90F) {
            locationNE = visibleRegion.farRight
            locationSW = visibleRegion.nearLeft

        } else if (bearing < 180F) {
            locationNE = visibleRegion.farLeft
            locationSW = visibleRegion.nearRight
        } else if (bearing < 270F) {
            locationNE = visibleRegion.nearLeft
            locationSW = visibleRegion.farRight
        } else {
            locationNE = visibleRegion.nearRight
            locationSW = visibleRegion.farLeft
        }

        val latNE = locationNE.latitude.toBigDecimal().toPlainString()
        val lngNE = locationNE.longitude.toBigDecimal().toPlainString()

        val latSW = locationSW.latitude.toBigDecimal().toPlainString()
        val lngSW = locationSW.longitude.toBigDecimal().toPlainString()

        val latUser = target.latitude.toBigDecimal().toPlainString()
        val lngUser = target.longitude.toBigDecimal().toPlainString()

        val zm = cameraPosition.zoom.toBigDecimal().toString()

        val queryParams = "ne_lat=$latNE&ne_lng=$lngNE&sw_lat=$latSW&sw_lng=$lngSW&user_latitude=$latUser&user_longitude=$lngUser&zoom=$zm"
        val token = access.Lime.token

        return Request.Builder()
            .url("${LIME_API_ENDPOINT}?$queryParams")
            .header("Authorization", "Bearer $token")
            .build()
    }



    private fun getBirdLogin1Req(endpoint:String, email:String): Request {
        val postBody = "{\"email\":\"$email\"}".toRequestBody(JSON_MEDIA_TYPE)

        return Request.Builder()
            .url("$BIRD_AUTH_ENDPOINT$endpoint")
            .header("User-Agent","Bird/4.119.0(co.bird.Ride; build:3; iOS 14.3.0) Alamofire/5.2.2")
            .addHeader("Device-Id", GUID.toString())
            .addHeader("Platform", "ios")
            .addHeader("App-Version", "4.119.0")
            .addHeader("Content-Type", "application/json")
            .post(postBody)
            .build()
    }

    private fun getBirdLogin2Req(endpoint:String, token:String): Request {
        val postBody = "{\"token\":\"$token\"}".toRequestBody(JSON_MEDIA_TYPE)

        return Request.Builder()
            .url("$BIRD_AUTH_ENDPOINT$endpoint")
            .header("User-Agent","Bird/4.119.0(co.bird.Ride; build:3; iOS 14.3.0) Alamofire/5.2.2")
            .addHeader("Device-Id", GUID.toString())
            .addHeader("Platform", "ios")
            .addHeader("App-Version", "4.119.0")
            .addHeader("Content-Type", "application/json")
            .post(postBody)
            .build()
    }

    private fun getLimeLogin1Req(): Request {
        var urlOut = LIME_AUTH_ENDPOINT

        if (access.Lime.methodLastUsed == "email" && access.Lime.email != null) {
            urlOut = "$urlOut?email=${access.Lime.email}"
        } else if (access.Lime.methodLastUsed == "phone" && access.Lime.phone != null) {
            urlOut = "$urlOut?phone=${access.Lime.phone}"
        }

        return Request.Builder()
            .url(urlOut)
            .build()
    }


    private fun getLimeLogin2Req(token:String): Request {
        var urlOut = LIME_AUTH_ENDPOINT

        val idInfo:String? = when (access.Lime.methodLastUsed) {
            "phone" -> access.Lime.phone
            "email" -> access.Lime.email
            else -> ""
        }

        val postBody = "{\"${access.Lime.methodLastUsed}\":\"$idInfo\", \"login_code\":\"$token\"}".toRequestBody(JSON_MEDIA_TYPE)

        return Request.Builder()
            .url(urlOut)
            .addHeader("Content-Type", "application/json")
            .post(postBody)
            .build()
    }

    private fun unpackBirdTokens(res: Response):Boolean {
        println("unpackTokens...")
        val tokens: AuthTokens =
            gson.fromJson(res.body!!.string(), AuthTokens::class.java)

        // unpack tokens
        access.Bird.refresh = tokens.refresh
        access.Bird.access = tokens.access

        println("tokens unpacked: ${access.Bird.refresh} ${access.Bird.access}")
        expires = (System.currentTimeMillis() / 1000L) + (60 * 60 * 24);
        return true
    }

    private fun unpackLimeTokens(res: Response) : Boolean {
        println("unpackTokens...")
        val authToken: AuthToken =
            gson.fromJson(res.body!!.string(), AuthToken::class.java)

        access.Lime.token = authToken.token

        return true
    }

    private fun getNearbyBirdReq(location:LatLng, radius:Number) : Request {
        val lat = location.latitude
        val lng = location.longitude

        val latStr = "%.5f".format(lat)
        val lngStr = "%.5f".format(lng)

        var altitude:Number? = 50
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
            .url("$BIRD_API_ENDPOINT?latitude=$latStr&longitude=$lngStr&radius=$radius")
            .header("User-Agent","Bird/4.119.0(co.bird.Ride; build:3; iOS 14.3.0) Alamofire/5.2.2")
            .addHeader("Device-id", GUID.toString())
            .addHeader("Platform", "ios")
            .addHeader("App-Version", "4.119.0")
            .addHeader("Content-Type", "application/json")
            .addHeader("legacyrequest", "false")
            .addHeader("Authorization", "Bearer $access")
            .addHeader("Location", gson.toJson(loc) )
            .build()
    }

//    private fun refreshTokens() : Boolean  {
//        if ((System.currentTimeMillis() / 1000L) > expires) {
//            val req = Request.Builder()
//                .url("$authEndpoint/refresh/token")
//                .header("User-Agent","Bird/4.119.0(co.bird.Ride; build:3; iOS 14.3.0) Alamofire/5.2.2")
//                .addHeader("Device-Id", GUID.toString())
//                .addHeader("Platform", "ios")
//                .addHeader("App-Version", "4.119.0")
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Authorization", "Bearer $refresh")
//                .build()
//            client.newCall(req).execute().use { res ->
//                if (!res.isSuccessful) throw IOException("Unexpected code $res")
//                else return unpackBirdTokens(res)
//            }
//        }
//        return false
//    }

}