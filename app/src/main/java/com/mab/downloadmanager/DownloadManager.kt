package com.mab.downloadmanager

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.*
import com.mab.downloadmanager.constants.StatusCode
import com.mab.downloadmanager.data.DownloadStatus
import com.mab.downloadmanager.data.LiveDataHelper
import java.io.File


/**
 * Created by Mirza Ahmed Baig on 2019-08-05.
 * Avantari Technologies
 * mirza@avantari.org
 */

class DownloadManager(private val context: Context) {

    private var downloadUpdateListener: DownloadUpdateListener? = null

    private val workMangersConstraints by lazy {
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }

    fun setListener(downloadUpdateListener: DownloadUpdateListener) {
        this.downloadUpdateListener = downloadUpdateListener
    }

    fun downloadFile(remoteUrl: String, outputFilePath: String) {

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
                            downloadUpdateListener?.onDownloadingUpdate(it.progress)
                        }
                        StatusCode.DOWNLOAD_SUCCESS -> {
                            downloadUpdateListener?.onDownloadingUpdate(it.progress)
                            downloadUpdateListener?.onDownloadingFinished()
                        }
                        StatusCode.DOWNLOAD_FAILED -> {
                            downloadUpdateListener?.onDownloadingFailed(it.errorMsg)
                            deleteFile(outputFilePath)
                        }
                    }
                }
            )
    }

    fun downloadFiles(remoteUrls: List<String>, outputFilePaths: List<String>) {

        if (remoteUrls.size != outputFilePaths.size)
            throw Exception("input urls should be equal to output file paths")


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
                            if (progressIndex >= 0) {
                                progressList[progressIndex].progress = it.progress
                                val progress = getProgress(progressList)
                                downloadUpdateListener?.onDownloadingUpdate(progress)
                                downloadUpdateListener?.onDownloadingUpdate(it.progress, progressIndex)
                            }
                        }
                        StatusCode.DOWNLOAD_SUCCESS -> {
                            val progressIndex = getIndexOfProgress(it.id, progressList)
                            if (progressIndex >= 0) {
                                progressList[progressIndex].progress = it.progress
                                if (isDownloadComplete(progressList)) {
                                    downloadUpdateListener?.onDownloadingUpdate(100f)
                                    remoteUrls.forEachIndexed { index, _ ->
                                        downloadUpdateListener?.onDownloadingUpdate(100f, index)
                                    }
                                    downloadUpdateListener?.onDownloadingFinished()
                                }
                            }
                        }
                        StatusCode.DOWNLOAD_FAILED -> {
                            downloadUpdateListener?.onDownloadingFailed(it.errorMsg)
                            outputFilePaths.forEach {
                                deleteFile(it)
                            }
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

    private fun deleteFile(path: String) {
        try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                file.delete()
            }
        } catch (e: java.lang.Exception) {
        }
    }
}