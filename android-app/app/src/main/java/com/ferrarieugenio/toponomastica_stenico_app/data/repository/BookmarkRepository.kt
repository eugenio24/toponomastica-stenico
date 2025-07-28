package com.ferrarieugenio.toponomastica_stenico_app.data.repository

import com.ferrarieugenio.toponomastica_stenico_app.data.datasource.BookmarkDataSource
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class BookmarkRepository @Inject constructor(
    private val dataSource: BookmarkDataSource
) {
    val bookmarkedIds: StateFlow<Set<Int>> = dataSource.bookmarkedIds

    fun toggle(id: Int) = dataSource.toggle(id)

    fun isBookmarked(id: Int): Boolean = dataSource.isBookmarked(id)
}
