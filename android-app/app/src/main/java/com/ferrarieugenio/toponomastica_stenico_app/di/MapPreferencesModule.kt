package com.ferrarieugenio.toponomastica_stenico_app.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.mapPreferencesDataStore by preferencesDataStore(name = "map_preferences")