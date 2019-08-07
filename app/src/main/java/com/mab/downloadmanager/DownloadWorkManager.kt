package com.mab.downloadmanager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mab.downloadmanager.constants.StatusCode
import com.mab.downloadmanager.models.DownloadStatus
import com.mab.downloadmanager.models.LiveDataHelper
import com.mab.downloadmanager.network.FileDownloadService
import com.mab.downloadmanager.network.OnFileDownloadListener
import com.mab.downloadmanager.network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import java.io.File


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
        downloadFile(url!!, destinationFilePath!!, object : OnFileDownloadListener {
            override fun onFileDownloadedError(message: String?) {
                Log.d(TAG, "onFileDownloadedError : $message")
                liveDataHelper?.updateStatus(DownloadStatus(id.toString(), -1f, StatusCode.DOWNLOAD_FAILED, message))
            }

            override fun onFileDownloadUpdate(percent: Float, totalSize: Long) {
                Log.d(TAG, "onFileDownloadUpdate : Id : $id $percent% $totalSize ${Thread.currentThread()}")
                if (percent != 100f)
                    liveDataHelper?.updateStatus(DownloadStatus(id.toString(), percent, StatusCode.DOWNLOADING))
            }


            override fun onFileCopyFinished() {
                Log.d(TAG, "onFileCopyFinished : ${Thread.currentThread()}")
                liveDataHelper?.updateStatus(DownloadStatus(id.toString(), 100f, StatusCode.DOWNLOAD_SUCCESS))
            }

        })
        return Result.success()
    }

    private fun downloadFile(url: String, destinationFilePath: String, listener: OnFileDownloadListener) {
        liveDataHelper = LiveDataHelper.getInstance()
        val retrofit = RetrofitClient.getDownloadRetrofit(listener)
        val downloadService = retrofit.create(FileDownloadService::class.java)

        val call = downloadService.downloadFileWithDynamicUrlSync(url)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                val bytes = response.body()?.bytes()
                Log.d(TAG, "Size : ${bytes?.size}")
                bytes?.let {
                    Log.d(TAG, "Thread Name : ${Thread.currentThread()}")
                    FileHelper.getFileSize(url) { size ->
                        if (size == it.size) {
                            val file = File(destinationFilePath)
                            file.writeBytes(it)
                            listener.onFileCopyFinished()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "onFailure : ${t.message}")
                listener.onFileDownloadedError(t.message)
            }
        })
    }
}