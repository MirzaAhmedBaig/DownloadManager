package com.mab.downloadmanager

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import okhttp3.ResponseBody
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import kotlin.math.roundToInt


/**
 * Created by Mirza Ahmed Baig on 2019-08-05.
 * Avantari Technologies
 * mirza@avantari.org
 */

object FileHelper {
    private val TAG = FileHelper::class.java.simpleName

    fun saveFileToDisk(
        body: ResponseBody,
        destinationFile: File,
        onProgress: (downLoadedSize: Long, totalSize: Long) -> Unit,
        onComplete: () -> Unit,
        onFailed: (String?) -> Unit
    ) {
        try {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                inputStream = body.byteStream()
                outputStream = FileOutputStream(destinationFile)
                val buffer = ByteArray((body.contentLength() / 100f).roundToInt())
                val fileSize = body.contentLength()
                var count = inputStream.read(buffer)
                var downloadedSize = 0L

                Log.d(TAG, "File Size=$fileSize")
                while (count > 0) {
                    outputStream.write(buffer, 0, count)
                    downloadedSize += count.toLong()
                    Log.d(
                        TAG,
                        "Progress: " + downloadedSize + "/" + fileSize + " >>>> " + downloadedSize.toFloat() / fileSize
                    )
                    onProgress(downloadedSize, fileSize)
                    count = inputStream.read(buffer)
                }
                outputStream.flush()
                Log.d(TAG, "Download Completed for file ${destinationFile.absolutePath}")
                onComplete()

            } catch (e: MalformedURLException) {
                e.printStackTrace()
                onFailed(e.message)
            } catch (e: Exception) {
                e.printStackTrace()
                onFailed(e.message)
            } catch (e: IOException) {
                e.printStackTrace()
                onFailed(e.message)
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            onFailed(e.message)
        }
    }

    fun getFileSize(url: String, onSizeFound: (Int) -> Unit) {
        val handlerThread = HandlerThread("myThread")
        handlerThread.start()
        Handler(handlerThread.looper).post {
            val urlConnection = URL(url).openConnection()
            urlConnection.connect()
            onSizeFound(urlConnection.contentLength)
        }
    }

    fun getFileSize(url: String): Int {
        val urlConnection = URL(url).openConnection()
        urlConnection.connect()
        return urlConnection.contentLength
    }
}