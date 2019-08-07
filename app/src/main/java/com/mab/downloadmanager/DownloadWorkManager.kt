package com.mab.downloadmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mab.downloadmanager.constants.StatusCode
import com.mab.downloadmanager.data.DownloadStatus
import com.mab.downloadmanager.data.LiveDataHelper
import com.mab.downloadmanager.network.OnDownloadUpdateListener
import com.mab.downloadmanager.network.retrofit.FileDownloadService
import com.mab.downloadmanager.network.retrofit.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import java.io.File
import java.io.IOException


/**
 * Created by Mirza Ahmed Baig on 2019-08-05.
 * Avantari Technologies
 * mirza@avantari.org
 */

class DownloadWorkManager(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    private val TAG = DownloadWorkManager::class.java.simpleName
    private var liveDataHelper: LiveDataHelper? = null
    override fun doWork(): Result {
        val url = inputData.getString("url")
        val destinationFilePath = inputData.getString("path")
        downloadFile(url!!, destinationFilePath!!, object : OnDownloadUpdateListener {
            override fun onFileDownloadedError(message: String?) {
                Log.e(TAG, "File downloading failed : $message")
                liveDataHelper?.updateStatus(DownloadStatus(id.toString(), -1f, StatusCode.DOWNLOAD_FAILED, message))
            }

            override fun onFileDownloadUpdate(percent: Float, totalSize: Long) {
                Log.d(TAG, "Downloaded  : $percent% of total size : $totalSize")
                if (percent != 100f)
                    liveDataHelper?.updateStatus(DownloadStatus(id.toString(), percent, StatusCode.DOWNLOADING))
            }


            override fun onFileCopyFinished() {
                liveDataHelper?.updateStatus(DownloadStatus(id.toString(), 100f, StatusCode.DOWNLOAD_SUCCESS))
            }

        })
        return Result.success()
    }

    private fun downloadFile(url: String, destinationFilePath: String, listener: OnDownloadUpdateListener) {
        liveDataHelper = LiveDataHelper.getInstance()
        val retrofit = RetrofitClient.getDownloadRetrofit(listener)
        val downloadService = retrofit.create(FileDownloadService::class.java)

        val call = downloadService.downloadFileWithDynamicUrl(url)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                try {
                    val bytes = response.body()?.bytes()
                    bytes?.let {
                        Log.d(TAG, "File download complete for URL : $url")
                        FileHelper.getFileSize(url) { size ->
                            if (size == it.size) {
                                Log.d(TAG, "File copying...")
                                try {
                                    val file = File(destinationFilePath)
                                    file.writeBytes(it)
                                    listener.onFileCopyFinished()
                                    Log.d(
                                        TAG,
                                        "File copy done written $size bytes successfully to $destinationFilePath"
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.e(TAG, "File copy failed \n ${e.message}")
                                }
                            } else {
                                Log.e(TAG, "File did not download successfully.")
                            }
                        }
                    } ?: Log.e(TAG, "Received null bytes from URL : ${response.code()}")
                } catch (e: IOException) {
                    Log.e(TAG, "File did not download successfully.")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "Unable to start downloading : ${t.message}")
                listener.onFileDownloadedError(t.message)
            }
        })
    }
}