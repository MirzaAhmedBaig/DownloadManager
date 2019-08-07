package com.mab.downloadmanager

import android.os.Handler
import android.os.HandlerThread
import java.net.URL


/**
 * Created by Mirza Ahmed Baig on 2019-08-05.
 * Avantari Technologies
 * mirza@avantari.org
 */

object FileHelper {
    fun getFileSize(url: String, onSizeFound: (Int) -> Unit) {
        val handlerThread = HandlerThread("myThread")
        handlerThread.start()
        Handler(handlerThread.looper).post {
            val urlConnection = URL(url).openConnection()
            urlConnection.connect()
            onSizeFound(urlConnection.contentLength)
        }
    }
}