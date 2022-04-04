package com.example.testmap.network.interfaces

import retrofit2.Retrofit
import com.google.gson.*
import java.util.UUID

interface BirdInterface {

    companion object {
        val baseURL = "https://api-bird.prod.birdapp.com/"
        fun create() : BirdInterface {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create()).baseURL(baseURL).build()
            return retrofit.create(BirdInterface::class.java)
        }
    }
}