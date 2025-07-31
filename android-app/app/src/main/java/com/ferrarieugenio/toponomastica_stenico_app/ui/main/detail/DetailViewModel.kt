package com.ferrarieugenio.toponomastica_stenico_app.ui.main.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.BookmarkRepository
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    val toponymRepository: ToponymRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _toponym = MutableLiveData<Toponym>()
    val toponym: LiveData<Toponym> = _toponym

    private val _neighbors = MutableLiveData<List<Toponym>>()
    val neighbors: LiveData<List<Toponym>> = _neighbors

    val isBookmarked: LiveData<Boolean> = combine(
        _toponym.asFlow(),
        bookmarkRepository.bookmarkedIds
    ) { toponym, bookmarkedIds ->
        toponym != null && bookmarkedIds.contains(toponym.id)
    }.asLiveData()

    fun setToponym(data: Toponym) {
        _toponym.value = data
        _neighbors.value = toponymRepository.getToponymNeighbors(_toponym.value!!)
    }

    fun toggleBookmark() {
        _toponym.value?.let { toponym ->
            viewModelScope.launch {
                bookmarkRepository.toggle(toponym.id)
            }
        }
    }
}