package com.ferrarieugenio.toponomastica_stenico_app.ui.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferrarieugenio.toponomastica_stenico_app.util.map.SatelliteDataManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SatelliteDataViewModel @Inject constructor(
    private val satelliteDataManager: SatelliteDataManager
) : ViewModel() {

    private val _isDataAvailable = MutableLiveData<Boolean>()
    val isDataAvailable: LiveData<Boolean> = _isDataAvailable

    private val _downloadProgress = MutableLiveData<Int>()
    val downloadProgress: LiveData<Int> = _downloadProgress

    private val _isDownloading = MutableLiveData<Boolean>(false)
    val isDownloading: LiveData<Boolean> = _isDownloading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        checkDataAvailability()
    }

    fun checkDataAvailability() {
        _isDataAvailable.value = satelliteDataManager.isSatelliteDataAvailable()
    }

    fun startDownload() {
        if (_isDownloading.value == true) return // already downloading

        _isDownloading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                satelliteDataManager.downloadAndExtractSatelliteData { percent ->
                    _downloadProgress.postValue(percent)
                }
                checkDataAvailability()
            } catch (e: Exception) {
                _errorMessage.postValue("Errore nel download: ${e.message ?: "Sconosciuto"}")
            } finally {
                _isDownloading.postValue(false)
            }
        }
    }

    fun deleteData() {
        viewModelScope.launch {
            val deleted = satelliteDataManager.satelliteDir.deleteRecursively()
            if (!deleted) {
                _errorMessage.postValue("Errore durante la rimozione dati")
            }
            checkDataAvailability()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
