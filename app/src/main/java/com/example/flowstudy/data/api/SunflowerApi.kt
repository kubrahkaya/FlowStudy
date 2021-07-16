package com.example.flowstudy.data.api

import com.example.flowstudy.data.local.Plant
import retrofit2.http.GET

interface SunflowerApi {

    @GET("googlecodelabs/kotlin-coroutines/master/advanced-coroutines-codelab/sunflower/src/main/assets/plants.json")
    suspend fun getAllPlants(): List<Plant>


    @GET("googlecodelabs/kotlin-coroutines/master/advanced-coroutines-codelab/sunflower/src/main/assets/custom_plant_sort_order.json")
    suspend fun getCustomPlantSortOder(): List<Plant>

}