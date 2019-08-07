package com.mab.downloadmanager.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel


/**
 * Created by Mirza Ahmed Baig on 2019-08-06.
 * Avantari Technologies
 * mirza@avantari.org
 */
class LiveDataHelper : ViewModel() {
    private val liveData = MediatorLiveData<DownloadStatus>()

    companion object {
        private var liveDataHelper: LiveDataHelper? = null
        @Synchronized
        fun getInstance(): LiveDataHelper {
            liveDataHelper = liveDataHelper ?: LiveDataHelper()
            return liveDataHelper!!
        }
    }

    fun updateStatus(downloadStatus: DownloadStatus) {
        this.liveData.postValue(downloadStatus)
    }

    fun observeStatus(): LiveData<DownloadStatus> {
        return liveData
    }


}