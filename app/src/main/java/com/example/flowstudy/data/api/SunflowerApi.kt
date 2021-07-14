package com.example.flowstudy.data.api

import com.example.flowstudy.data.local.Plant
import retrofit2.http.GET

interface SunflowerApi {

    @GET("app/src/main/assets/plants.json")
    suspend fun getAllPlants(): List<Plant>


    @GET("app/src/main/assets/plants.json")
    suspend fun getCustomPlantSortOder(): List<Plant>

}