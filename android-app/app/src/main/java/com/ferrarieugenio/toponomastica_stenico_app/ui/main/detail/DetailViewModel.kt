package com.ferrarieugenio.toponomastica_stenico_app.ui.main.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: ToponymRepository
) : ViewModel() {

    private val _toponym = MutableLiveData<Toponym>()
    val toponym: LiveData<Toponym> = _toponym

    private val _neighbors = MutableLiveData<List<Toponym>>()
    val neighbors: LiveData<List<Toponym>> = _neighbors

    fun setToponym(data: Toponym) {
        _toponym.value = data
        _neighbors.value = repository.getToponymNeighbors(_toponym.value!!)
    }
}