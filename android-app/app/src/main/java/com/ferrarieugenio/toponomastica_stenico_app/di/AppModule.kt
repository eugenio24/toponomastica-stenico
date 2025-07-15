package com.ferrarieugenio.toponomastica_stenico_app.di

import android.content.Context
import com.ferrarieugenio.toponomastica_stenico_app.data.datasource.ToponymAssetDataSource
import com.ferrarieugenio.toponomastica_stenico_app.data.repository.ToponymRepository
import com.ferrarieugenio.toponomastica_stenico_app.util.map.SatelliteDataManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideToponymDataSource(
        @ApplicationContext context: Context
    ): ToponymAssetDataSource = ToponymAssetDataSource(context)

    @Provides
    fun provideFilename(): String = "toponimi.json"

    @Provides
    @Singleton
    fun provideToponymRepository(
        dataSource: ToponymAssetDataSource,
        filename: String
    ): ToponymRepository = ToponymRepository(dataSource, filename)

    @Provides
    @Singleton
    fun provideSatelliteDataManager(
        @ApplicationContext context: Context
    ): SatelliteDataManager = SatelliteDataManager(context)
}