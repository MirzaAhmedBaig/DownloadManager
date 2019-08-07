package com.mab.downloadmanager


/**
 * Created by Mirza Ahmed Baig on 2019-08-07.
 * Avantari Technologies
 * mirza@avantari.org
 */

interface DownloadUpdateListener {
    fun onDownloadingFailed(message: String?) {}
    fun onDownloadingUpdate(percent: Float) {}
    fun onDownloadingUpdate(percent: Float, urlIndex: Int) {}
    fun onDownloadingFinished() {}
}