package com.mab.downloadmanager.network


/**
 * Created by Mirza Ahmed Baig on 2019-08-05.
 * Avantari Technologies
 * mirza@avantari.org
 */

interface OnDownloadUpdateListener {
    fun onFileDownloadedError(message: String?)
    fun onFileDownloadUpdate(percent: Float, totalSize: Long)
    fun onFileCopyFinished()
}