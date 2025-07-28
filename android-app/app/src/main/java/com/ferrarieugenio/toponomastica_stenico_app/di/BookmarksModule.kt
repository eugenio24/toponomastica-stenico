package com.ferrarieugenio.toponomastica_stenico_app.di

import android.content.Context
import com.ferrarieugenio.toponomastica_stenico_app.data.datasource.BookmarkDataSource
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.BookmarkRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookmarksModule {

    @Provides
    @Singleton
    fun provideBookmarkDataSource(
        @ApplicationContext context: Context
    ): BookmarkDataSource = BookmarkDataSource(context)

    @Provides
    @Singleton
    fun provideBookmarkRepository(
        dataSource: BookmarkDataSource
    ): BookmarkRepository = BookmarkRepository(dataSource)
}