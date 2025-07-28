package com.ferrarieugenio.toponomastica_stenico_app.di

import android.content.Context
import com.ferrarieugenio.toponomastica_stenico_app.util.download.SatelliteDataManager
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
    fun provideSatelliteDataManager(
        @ApplicationContext context: Context
    ): SatelliteDataManager = SatelliteDataManager(context)
}