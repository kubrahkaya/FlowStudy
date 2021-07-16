package com.example.flowstudy.ui.fragment

import androidx.lifecycle.*
import com.example.flowstudy.data.local.GrowZone
import com.example.flowstudy.data.local.NoGrowZone
import com.example.flowstudy.data.local.Plant
import com.example.flowstudy.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@HiltViewModel
class PlantListViewModel @Inject constructor(private val plantRepository: PlantRepository) :
    ViewModel() {

    private val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?>
        get() = _snackbar

    private val _spinner = MutableLiveData(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val growZoneFlow = MutableStateFlow(NoGrowZone)

    val plantsUsingFlow: LiveData<List<Plant>> = growZoneFlow.flatMapLatest { growZone ->
        if (growZone == NoGrowZone) {
            plantRepository.plantsFlow
        } else {
            plantRepository.getPlantsWithGrowZoneFlow(growZone)
        }
    }.asLiveData()

    init {
        clearGrowZoneNumber()
        growZoneFlow.mapLatest { growZone ->
            _spinner.value = true
            if (growZone == NoGrowZone) {
                plantRepository.tryUpdateRecentPlantsCache()
            } else {
                plantRepository.tryUpdateRecentPlantsForGrowZoneCache(growZone)
            }
        }.onEach {
            _spinner.value = false
        }.catch { _snackbar.value = it.message }
            .launchIn(viewModelScope)
    }

    fun setGrowZoneNumber(num: Int) {
        growZoneFlow.value = GrowZone(num)
    }

    fun clearGrowZoneNumber() {
        growZoneFlow.value = NoGrowZone
    }

    fun isFiltered() = growZoneFlow.value != NoGrowZone

    fun onSnackbarShown() {
        _snackbar.value = null
    }

}