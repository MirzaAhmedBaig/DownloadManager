package com.mab.downloadmanager

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), DownloadUpdateListener {

    private val TAG = MainActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        single_download.setOnClickListener {
            downloadSingleFile()
        }
        multi_download.setOnClickListener {
            downloadMultipleFiles()
        }
    }

    private val urls = listOf(
        "http://www.noiseaddicts.com/samples_1w72b820/280.mp3",
        "https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_1MG.mp3"
    )
    private val paths by lazy {
        listOf(
            File(Environment.getExternalStorageDirectory(), "firstFile.mp3").absolutePath,
            File(getExternalFilesDir(null), "secondFile.mp3").absolutePath
        )
    }

    private fun downloadMultipleFiles() {
        DownloadManager(this).apply {
            downloadFiles(urls, paths)
            setListener(this@MainActivity)
        }
    }

    private fun downloadSingleFile() {
        DownloadManager(this).apply {
            downloadFile(urls[0], paths[0])
            setListener(this@MainActivity)
        }
    }

    override fun onDownloadingFailed(message: String?) {
        Toast.makeText(this, "Error : $message", Toast.LENGTH_SHORT).show()
    }

    override fun onDownloadingUpdate(percent: Float) {
        progress_bar.progress = percent.roundToInt()
        progress_value.text = "$percent%"
    }

    override fun onDownloadingFinished() {
        Toast.makeText(this, "Downloading Completed", Toast.LENGTH_SHORT).show()

    }

    override fun onDownloadingUpdate(percent: Float, urlIndex: Int) {
        progressArray[urlIndex] = percent
        showMultiProgressStatus()

    }

    private val progressArray = ArrayList<Float>().apply {
        add(0f)
        add(0f)
    }

    private fun showMultiProgressStatus() {
        progress_status.text =
            "Progress for index 0 : ${progressArray[0]}%\n\nProgress for index 1 : ${progressArray[1]}%"
    }

}
