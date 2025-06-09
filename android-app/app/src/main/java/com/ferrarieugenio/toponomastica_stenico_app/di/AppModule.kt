package com.ferrarieugenio.toponomastica_stenico_app.di

import android.content.Context
import com.ferrarieugenio.toponomastica_stenico_app.data.datasource.ToponymAssetDataSource
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideToponymDataSource(
        @ApplicationContext context: Context
    ): ToponymAssetDataSource = ToponymAssetDataSource(context)

    @Provides
    fun provideToponymRepository(
        dataSource: ToponymAssetDataSource
    ): ToponymRepository = ToponymRepository(dataSource)
}