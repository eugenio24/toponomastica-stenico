package com.ferrarieugenio.toponomastica_stenico_app.ui.main.map

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: ToponymRepository
) : ViewModel() {

    private val _toponyms = MutableLiveData<List<Toponym>>()
    val toponyms: LiveData<List<Toponym>> = _toponyms

    init {
        viewModelScope.launch {
            val list = repository.getToponyms()
            _toponyms.postValue(list)
        }
    }
}
