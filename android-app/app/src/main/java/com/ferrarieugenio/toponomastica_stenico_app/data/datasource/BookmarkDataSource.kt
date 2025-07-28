package com.ferrarieugenio.toponomastica_stenico_app.data.datasource

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class BookmarkDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
    private val key = "bookmarked_ids"

    private val _bookmarkedIds = MutableStateFlow(loadBookmarks())
    val bookmarkedIds: StateFlow<Set<Int>> = _bookmarkedIds

    private fun loadBookmarks(): Set<Int> {
        val stringSet = prefs.getStringSet(key, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun toggle(id: Int) {
        val updated = _bookmarkedIds.value.toMutableSet().apply {
            if (contains(id)) remove(id) else add(id)
        }
        val stringSet = updated.map { it.toString() }.toSet()
        prefs.edit { putStringSet(key, stringSet) }
        _bookmarkedIds.value = updated
    }

    fun isBookmarked(id: Int): Boolean = _bookmarkedIds.value.contains(id)
}