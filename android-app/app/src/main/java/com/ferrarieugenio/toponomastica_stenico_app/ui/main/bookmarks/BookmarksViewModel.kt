package com.ferrarieugenio.toponomastica_stenico_app.ui.main.bookmarks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ferrarieugenio.toponomastica_stenico_app.data.model.Toponym
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.BookmarkRepository
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val toponymRepository: ToponymRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _toponyms = MutableLiveData<List<Toponym>>()

    private val _bookmarkedToponyms = MediatorLiveData<List<Toponym>>()
    val bookmarkedToponyms: LiveData<List<Toponym>> get() = _bookmarkedToponyms

    private var currentToponyms: List<Toponym>? = null
    private var currentBookmarks: Set<Int>? = null

    private fun update() {
        val toponyms: List<Toponym>? = currentToponyms
        val bookmarks: Set<Int>? = currentBookmarks
        if (toponyms != null && bookmarks != null) {
            val filtered: List<Toponym> = toponyms.filter { bookmarks.contains(it.id) }
            _bookmarkedToponyms.value = filtered
        }
    }

    init {
        viewModelScope.launch {
            val toponyms = toponymRepository.getToponyms()
            _toponyms.postValue(toponyms)
        }

        _bookmarkedToponyms.addSource(_toponyms) { toponyms ->
            currentToponyms = toponyms
            update()
        }

        _bookmarkedToponyms.addSource(bookmarkRepository.bookmarkedIds.asLiveData()) { bookmarks ->
            currentBookmarks = bookmarks
            update()
        }
    }

    fun getToponymById(id: Int) : Toponym? {
        return bookmarkedToponyms.value?.find { it.id == id }
    }

    fun toggleBookmark(id: Int) {
        bookmarkRepository.toggle(id)
    }
}
