package com.mab.downloadmanager

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.*
import com.mab.downloadmanager.constants.StatusCode
import com.mab.downloadmanager.models.DownloadStatus
import com.mab.downloadmanager.models.LiveDataHelper


/**
 * Created by Mirza Ahmed Baig on 2019-08-05.
 * Avantari Technologies
 * mirza@avantari.org
 */

object DownloadManager {

    private val TAG = "DownloadManager"

    private val workMangersConstraints by lazy {
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }

    fun downloadFile(
        context: Context,
        remoteUrl: String, outputFilePath: String, onProgress: (progress: Float) -> Unit,
        onComplete: () -> Unit,
        onFailed: (String?) -> Unit
    ) {

        val data = workDataOf(Pair("url", remoteUrl), Pair("path", outputFilePath))

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(DownloadWorkManager::class.java)
            .setConstraints(workMangersConstraints)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)

        LiveDataHelper.getInstance().observeStatus()
            .observe(context as LifecycleOwner,
                Observer<DownloadStatus> {
                    when (it.status) {
                        StatusCode.DOWNLOADING -> {
                            Log.d("#TAG", "Progress : ${it.progress}")
                            onProgress(it.progress)
                        }
                        StatusCode.DOWNLOAD_SUCCESS -> {
                            onProgress(it.progress)
                            onComplete()
                        }
                        StatusCode.DOWNLOAD_FAILED -> {
                            onFailed(it.errorMsg)
                        }
                    }
                }
            )
    }

    fun downloadFiles(
        context: Context,
        remoteUrls: List<String>, outputFilePaths: List<String>, onProgress: (progress: Float) -> Unit,
        onComplete: () -> Unit,
        onFailed: (String?) -> Unit
    ) {

        if (remoteUrls.size != outputFilePaths.size)
            throw Exception("input urls should be equal to output file paths")

        var totalSize = 0
        remoteUrls.forEachIndexed { index, url ->
            FileHelper.getFileSize(url) {
                totalSize += it
                if (index == remoteUrls.lastIndex) {
                    (context as AppCompatActivity).runOnUiThread {
                        startMultipleDownloads(
                            context,
                            remoteUrls,
                            outputFilePaths,
                            totalSize,
                            onProgress,
                            onComplete,
                            onFailed
                        )
                    }
                }
            }
        }
    }


    private fun startMultipleDownloads(
        context: Context,
        remoteUrls: List<String>,
        outputFilePaths: List<String>,
        totalFileSize: Int,
        onProgress: (progress: Float) -> Unit,
        onComplete: () -> Unit,
        onFailed: (String?) -> Unit
    ) {
        val progressList = ArrayList<DownloadStatus>()
        val requestList = ArrayList<WorkRequest>()
        remoteUrls.forEachIndexed { index, url ->
            val data = workDataOf(Pair("url", url), Pair("path", outputFilePaths[index]))

            val oneTimeWorkRequest = OneTimeWorkRequest.Builder(DownloadWorkManager::class.java)
                .setConstraints(workMangersConstraints)
                .setInputData(data)
                .build()
            requestList.add(oneTimeWorkRequest)
            progressList.add(DownloadStatus(oneTimeWorkRequest.id.toString(), 0f, 0, null))
        }

        WorkManager.getInstance(context).enqueue(requestList)

        LiveDataHelper.getInstance().observeStatus()
            .observe(context as LifecycleOwner,
                Observer<DownloadStatus> {
                    when (it.status) {
                        StatusCode.DOWNLOADING -> {
                            val progressIndex = getIndexOfProgress(it.id, progressList)
                            Log.d(TAG, "Progress : ${it.progress} For : $progressIndex")
                            progressList[progressIndex].progress = it.progress
                            val progress = getProgress(progressList)
                            Log.d(TAG, "AVG Progress : $progress")
                            onProgress(progress)
                        }
                        StatusCode.DOWNLOAD_SUCCESS -> {
                            val progressIndex = getIndexOfProgress(it.id, progressList)
                            progressList[progressIndex].progress = it.progress
                            val progress = getProgress(progressList)
                            Log.d(TAG, "DOWNLOAD_SUCCESS $progress ${progressList.map { it.progress }.average()}")
                            if (isDownloadComplete(progressList)) {
                                onProgress(100f)
                                onComplete()
                            }
                        }
                        StatusCode.DOWNLOAD_FAILED -> {
                            onFailed(it.errorMsg)
                        }
                    }
                }
            )

    }

    private fun getIndexOfProgress(id: String, progressList: List<DownloadStatus>): Int {
        return progressList.indexOfFirst { it.id == id }
    }

    private fun getProgress(progressList: List<DownloadStatus>): Float {
        return progressList.map { it.progress }.average().toFloat()

    }

    private fun isDownloadComplete(progressList: List<DownloadStatus>): Boolean {
        return (progressList.map { it.progress }.average().toInt() == 100)
    }

}