package com.ferrarieugenio.toponomastica_stenico_app.ui.main.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ToponymRepository
) : ViewModel() {

    private val _allToponyms = MutableLiveData<List<Toponym>>()
    val filteredToponyms = MutableLiveData<List<Toponym>>()

    init {
        viewModelScope.launch {
            val data = repository.getToponyms()
            _allToponyms.value = data
            filteredToponyms.value = data
        }
    }

    fun filter(query: String) {
        val originalList = _allToponyms.value ?: return
        val lowerQuery = query.lowercase()

        filteredToponyms.value = originalList.filter {
            it.nome.lowercase().contains(lowerQuery) ||
                    it.descrizione.lowercase().contains(lowerQuery)
        }
    }
}