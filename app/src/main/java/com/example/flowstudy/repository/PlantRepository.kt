package com.example.flowstudy.repository

import androidx.annotation.AnyThread
import com.example.flowstudy.data.api.SunflowerApi
import com.example.flowstudy.data.local.GrowZone
import com.example.flowstudy.data.local.NoGrowZone
import com.example.flowstudy.data.local.Plant
import com.example.flowstudy.data.local.PlantDao
import com.example.flowstudy.util.CacheOnSuccess
import com.example.flowstudy.util.ComparablePair
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class PlantRepository @Inject constructor(
    private val plantDao: PlantDao,
    private val sunflowerApi: SunflowerApi,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private var plantsListSortOrderCache = CacheOnSuccess(onErrorFallback = { listOf() }) {
        withContext(Dispatchers.Default) {
            val result = sunflowerApi.getCustomPlantSortOder()
            result.map { plant -> plant.plantId }
        }
    }

    private val customSortFlow = plantsListSortOrderCache::getOrAwait.asFlow()

    val plantsFlow: Flow<List<Plant>>
        get() = plantDao.getPlantsFlow()
            .combine(customSortFlow) { plants, sortOrder ->
                plants.applySort(sortOrder)
            }
            .flowOn(defaultDispatcher)
            .conflate()

    @AnyThread
    private suspend fun List<Plant>.applyMainSafeSort(customSortOrder: List<String>) =
        withContext(defaultDispatcher) {
            this@applyMainSafeSort.applySort(customSortOrder)
        }

    private fun List<Plant>.applySort(customSortOder: List<String>): List<Plant> {
        return sortedBy { plant ->
            val positionForItem = customSortOder.indexOf(plant.plantId).let { order ->
                if (order > -1) order else Int.MAX_VALUE
            }
            ComparablePair(positionForItem, plant.name)
        }
    }

    fun getPlantsWithGrowZoneFlow(growZone: GrowZone): Flow<List<Plant>> {
        return plantDao.getPlantsWithGrowZoneNumberFlow(growZone.number)
            .map { plantList ->
                val sortOrderFromNetwork = plantsListSortOrderCache.getOrAwait()
                val nextValue = plantList.applyMainSafeSort(sortOrderFromNetwork)
                nextValue
            }
    }

    suspend fun tryUpdateRecentPlantsForGrowZoneCache(growZoneNumber: GrowZone) {
        if (shouldUpdatePlantsCache(growZoneNumber)) fetchPlantsForGrowZone(growZoneNumber)
    }

    suspend fun tryUpdateRecentPlantsCache() {
        if (shouldUpdatePlantsCache(NoGrowZone)) fetchRecentPlants()
    }

    private suspend fun fetchRecentPlants() {
        val plants = allPlants()
        plantDao.insertAll(plants)
    }

    suspend fun allPlants(): List<Plant> = withContext(Dispatchers.Default) {
        delay(1500)
        val result = sunflowerApi.getAllPlants()
        result.shuffled()
    }

    private suspend fun fetchPlantsForGrowZone(growZoneNumber: GrowZone): List<Plant> {
        val plants = plantsByGrowZone(growZoneNumber)
        plantDao.insertAll(plants)
        return plants
    }

    suspend fun plantsByGrowZone(growZone: GrowZone) = withContext(Dispatchers.Default) {
        delay(1500)
        val result = sunflowerApi.getAllPlants()
        result.filter { it.growZoneNumber == growZone.number }.shuffled()
    }

    private suspend fun shouldUpdatePlantsCache(growZone: GrowZone): Boolean {
        // suspending function, so you can e.g. check the status of the database here
        return true
    }


}