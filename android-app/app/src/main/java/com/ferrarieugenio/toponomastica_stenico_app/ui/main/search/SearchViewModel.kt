package com.ferrarieugenio.toponomastica_stenico_app.ui.main.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import com.ferrarieugenio.toponomastica_stenico_app.util.AdvancedFilters
import com.ferrarieugenio.toponomastica_stenico_app.util.SortDirection
import com.ferrarieugenio.toponomastica_stenico_app.util.SortField
import com.ferrarieugenio.toponomastica_stenico_app.util.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ToponymRepository
) : ViewModel() {

    lateinit var availableTags: List<String>
    lateinit var availableClusters: List<String>
    lateinit var availableHcClusters: List<String>

    private val _allToponyms = MutableLiveData<List<Toponym>>()
    val filteredToponyms = MutableLiveData<List<Toponym>>()

    private val _currentSortOption = MutableLiveData(SortOption())
    val currentSortOption: LiveData<SortOption> get() = _currentSortOption

    private val _currentQuery = MutableLiveData("")
    val currentQuery: LiveData<String> get() = _currentQuery

    private val _currentFilters = MutableLiveData(AdvancedFilters())
    val currentFilters: LiveData<AdvancedFilters> get() = _currentFilters

    init {
        viewModelScope.launch {
            val data = repository.getToponyms()
            _allToponyms.value = data
            filteredToponyms.value = data

            availableTags = repository.getAvailableTags()
            availableClusters = repository.getAvailableClusters()
            availableHcClusters = repository.getAvailableHcClusters()
        }
    }

    fun filter(query: String? = null, filters: AdvancedFilters? = null) {
        val originalList = _allToponyms.value ?: return

        val usedQuery = query ?: currentQuery.value.orEmpty()
        val usedFilters = filters ?: currentFilters.value ?: AdvancedFilters()

        _currentQuery.value = usedQuery
        _currentFilters.value = usedFilters

        val lowerQuery = usedQuery.lowercase()

        val filtered = originalList.filter { toponym ->
            toponym.matchesQuery(lowerQuery) && toponym.matchesFilters(usedFilters)
        }

        filteredToponyms.value = applySort(filtered, _currentSortOption.value ?: SortOption())
    }

    private fun Toponym.matchesQuery(query: String): Boolean {
        return nome.lowercase().contains(query) ||
                forma_ufficiale?.lowercase()?.contains(query) == true ||
                varianti?.any { it.lowercase().contains(query) } == true ||
                descrizione.lowercase().contains(query)
    }

    private fun Toponym.matchesFilters(filters: AdvancedFilters): Boolean {
        return (filters.minQuota == null || quota >= filters.minQuota) &&
                (filters.maxQuota == null || quota <= filters.maxQuota) &&
                (filters.selectedTags.isEmpty() || filters.selectedTags.any { it in tags }) &&
                (filters.selectedClusters.isEmpty() || filters.selectedClusters.any { it in cluster }) &&
                (filters.selectedHcClusters.isEmpty() || filters.selectedHcClusters.any { it in hc_cluster })
    }

    fun setSortOption(newOption: SortOption) {
        _currentSortOption.value = newOption
        sort()
    }

    private fun sort() {
        val currentList = filteredToponyms.value ?: emptyList()
        val option = _currentSortOption.value ?: SortOption()
        filteredToponyms.value = applySort(currentList, option)
    }

    private fun applySort(list: List<Toponym>, sortOption: SortOption): List<Toponym> {
        return when (sortOption.field) {
            SortField.NAME -> {
                if (sortOption.direction == SortDirection.ASCENDING)
                    list.sortedBy { it.nome.lowercase() }
                else
                    list.sortedByDescending { it.nome.lowercase() }
            }
            SortField.QUOTA -> {
                if (sortOption.direction == SortDirection.ASCENDING)
                    list.sortedBy { it.quota }
                else
                    list.sortedByDescending { it.quota }
            }
        }
    }
}