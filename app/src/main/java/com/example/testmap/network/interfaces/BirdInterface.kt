package com.example.testmap.network.interfaces
import com.example.testmap.network.models.NearbyScooterResponse
import retrofit2.Call

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.UUID

interface BirdInterface {

    @POST("/bird/nearby")
    fun getNearbyBirds(@Query("latitude") latitude:Float,
                       @Query("longitude") longitude:Float,
                       @Query("radius") radius:Int,
                       @Header("Authorization") auth:String,
                       @Header("Device-Id") guid:String,
                       @Header("Location") locMapString:String,
                       @Header("User-Agent") ua:String="Bird/4.119.0(co.bird.Ride; build:3; iOS 14.3.0) Alamofire/5.2.2",
                       @Header("legacyrequest") lr:Boolean=false,
                       @Header("App-Version") appVersion:String="4.119.0"
    ) : Call<NearbyScooterResponse>

    companion object {
        private const val apiURL = "https://api-bird.prod.birdapp.com/"
        private val GUID = UUID.randomUUID();
        private var access = ""
        private var refresh = ""
        //private val aut_url = "https://api-auth.prod.birdapp.com/api/v1/auth/"


        fun create() : BirdInterface {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create()).baseUrl(apiURL).build()
            return retrofit.create(BirdInterface::class.java)
        }


    }
}