package com.mab.downloadmanager.models


/**
 * Created by Mirza Ahmed Baig on 2019-08-06.
 * Avantari Technologies
 * mirza@avantari.org
 */
data class DownloadStatus(var id: String, var progress: Float, var status: Int, var errorMsg: String? = null)