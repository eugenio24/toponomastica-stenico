package com.ferrarieugenio.toponomastica_stenico_app.ui.main.settings.satellitedata

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ferrarieugenio.toponomastica_stenico_app.util.download.SatelliteDataManager
import com.ferrarieugenio.toponomastica_stenico_app.util.download.SatelliteDownloadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SatelliteDataViewModel @Inject constructor(
    private val satelliteDataManager: SatelliteDataManager,
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val DOWNLOAD_WORK_TAG = "satellite_download"
        private const val KEY_WORK_ID = "workId"
        private const val KEY_START_TIME = "downloadStartTime"
    }

    private val _isDataAvailable = MutableLiveData<Boolean>()
    val isDataAvailable: LiveData<Boolean> = _isDataAvailable

    private val _downloadProgress = MutableLiveData(0)
    val downloadProgress: LiveData<Int> = _downloadProgress

    private val _isDownloading = MutableLiveData(false)
    val isDownloading: LiveData<Boolean> = _isDownloading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _estimatedTimeRemaining = MutableLiveData<String?>()
    val estimatedTimeRemaining: LiveData<String?> = _estimatedTimeRemaining

    private var workId: UUID? = null
    private var downloadObserver: Observer<WorkInfo?>? = null
    private var startTimeMillis: Long? = null
    private var hasHandledCompletion = false

    init {
        restoreStateOrDetectRunningWork()
        checkDataAvailability()
    }

    private fun restoreStateOrDetectRunningWork() {
        workId = savedStateHandle.get<String>(KEY_WORK_ID)?.let {
            runCatching { UUID.fromString(it) }.getOrNull()
        }
        startTimeMillis = savedStateHandle.get<Long>(KEY_START_TIME)

        if (workId != null) {
            observeDownloadWorker()
            _isDownloading.value = true
        } else {
            try {
                val infos = WorkManager.getInstance(context)
                    .getWorkInfosByTag(DOWNLOAD_WORK_TAG)
                    .get()
                val activeWork = infos.firstOrNull {
                    it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
                }
                if (activeWork != null) {
                    workId = activeWork.id
                    savedStateHandle[KEY_WORK_ID] = workId.toString()

                    // Try to get start time from work progress or fallback
                    val progressStartTime = activeWork.progress.getLong("downloadStartTime", 0L)
                    if (progressStartTime > 0L) {
                        startTimeMillis = progressStartTime
                    } else if (!savedStateHandle.contains(KEY_START_TIME)) {
                        val now = System.currentTimeMillis()
                        savedStateHandle[KEY_START_TIME] = now
                        startTimeMillis = now
                    }

                    observeDownloadWorker()
                    _isDownloading.value = true
                } else {
                    resetDownloadState()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                resetDownloadState()
            }
        }
    }

    fun checkDataAvailability() {
        _isDataAvailable.value = satelliteDataManager.isSatelliteDataAvailable()
    }

    fun startDownload() {
        if (_isDownloading.value == true) return

        val request = OneTimeWorkRequestBuilder<SatelliteDownloadWorker>()
            .addTag(DOWNLOAD_WORK_TAG)
            .build()

        workId = request.id
        savedStateHandle[KEY_WORK_ID] = workId.toString()
        val now = System.currentTimeMillis()
        startTimeMillis = now
        savedStateHandle[KEY_START_TIME] = now

        hasHandledCompletion = false
        _isDownloading.value = true
        _errorMessage.value = null
        _downloadProgress.value = 0
        _estimatedTimeRemaining.value = null

        WorkManager.getInstance(context).enqueue(request)

        observeDownloadWorker()
    }

    private fun observeDownloadWorker() {
        workId?.let { id ->
            // Remove previous observer if any
            downloadObserver?.let { previousObserver ->
                WorkManager.getInstance(context)
                    .getWorkInfoByIdLiveData(id)
                    .removeObserver(previousObserver)
            }

            val observer = Observer<WorkInfo?> { info ->
                if (info != null) {
                    when (info.state) {
                        WorkInfo.State.RUNNING -> {
                            _isDownloading.postValue(true)
                            val progress = info.progress.getInt("progress", 0)

                            // Update startTimeMillis if worker sent a start time
                            val workerStartTime = info.progress.getLong("downloadStartTime", 0L)
                            if (workerStartTime > 0L) {
                                startTimeMillis = workerStartTime
                                savedStateHandle[KEY_START_TIME] = workerStartTime
                            }

                            _downloadProgress.postValue(progress)
                            updateTimeEstimate(progress)
                        }

                        WorkInfo.State.SUCCEEDED -> {
                            if (!hasHandledCompletion) {
                                hasHandledCompletion = true
                                _isDownloading.postValue(false)
                                _downloadProgress.postValue(100)
                                resetDownloadState()
                                checkDataAvailability()
                            }
                        }

                        WorkInfo.State.FAILED -> {
                            if (!hasHandledCompletion) {
                                hasHandledCompletion = true
                                _isDownloading.postValue(false)
                                val msg = info.outputData.getString("error") ?: "Errore sconosciuto"
                                _errorMessage.postValue("Errore: $msg")
                                resetDownloadState()
                            }
                        }

                        WorkInfo.State.CANCELLED -> {
                            if (!hasHandledCompletion) {
                                hasHandledCompletion = true
                                _isDownloading.postValue(false)
                                _errorMessage.postValue("Download annullato")
                                resetDownloadState()
                            }
                        }

                        else -> {}
                    }
                }
            }

            downloadObserver = observer
            WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(id)
                .observeForever(observer)
        }
    }

    private fun updateTimeEstimate(progress: Int) {
        val start = startTimeMillis ?: return
        if (progress <= 10) {
            _estimatedTimeRemaining.postValue("Calcolo del tempo rimanente...")
            return
        }

        val elapsedMillis = System.currentTimeMillis() - start
        val estimatedTotal = (elapsedMillis / progress.toDouble()) * 100
        val remainingMillis = estimatedTotal - elapsedMillis

        if (remainingMillis <= 0) {
            _estimatedTimeRemaining.postValue("Calcolo stima...")
            return
        }

        val minutes = (remainingMillis / 60000).toInt()
        val seconds = ((remainingMillis % 60000) / 1000).toInt()

        _estimatedTimeRemaining.postValue(
            if (minutes > 0)
                "$minutes min ${seconds}s rimanenti"
            else
                "$seconds secondi rimanenti"
        )
    }

    fun cancelDownload() {
        workId?.let {
            WorkManager.getInstance(context).cancelWorkById(it)
            downloadObserver?.let { observer ->
                WorkManager.getInstance(context)
                    .getWorkInfoByIdLiveData(it)
                    .removeObserver(observer)
            }
            downloadObserver = null
        }
    }

    fun deleteData() {
        if (_isDownloading.value == true) {
            _errorMessage.postValue("Impossibile eliminare i dati durante il download")
            return
        }

        viewModelScope.launch {
            val deleted = satelliteDataManager.deleteSatelliteData()
            if (!deleted) {
                _errorMessage.postValue("Errore durante la rimozione dei dati")
            }
            checkDataAvailability()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun resetDownloadState() {
        savedStateHandle.remove<String>(KEY_WORK_ID)
        savedStateHandle.remove<Long>(KEY_START_TIME)
        workId = null
        startTimeMillis = null
        hasHandledCompletion = false
        _isDownloading.postValue(false)
        _downloadProgress.postValue(0)
        _estimatedTimeRemaining.postValue(null)
    }

    override fun onCleared() {
        super.onCleared()
        downloadObserver?.let {
            workId?.let { id ->
                WorkManager.getInstance(context)
                    .getWorkInfoByIdLiveData(id)
                    .removeObserver(it)
            }
        }
        downloadObserver = null
    }
}
