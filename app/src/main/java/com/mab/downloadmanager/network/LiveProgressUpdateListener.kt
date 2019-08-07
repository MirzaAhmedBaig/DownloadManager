package com.mab.downloadmanager.network


/**
 * Created by Mirza Ahmed Baig on 2019-08-06.
 * Avantari Technologies
 * mirza@avantari.org
 */
interface LiveProgressUpdateListener {
    fun onSuccess()
    fun onFailed(message: String?)
    fun onUpdate(percent: Int, totalSize: Long)
    fun onFinished()
}