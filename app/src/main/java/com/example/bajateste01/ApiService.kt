package com.example.bajateste01

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @GET("api")
    fun getDados(): Call<Dados>
}