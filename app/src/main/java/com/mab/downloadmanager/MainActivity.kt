package com.mab.downloadmanager

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            getFileInfo()
//            startActivity(Intent(this, Main2Activity::class.java))
        }
    }

    val url = "https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_2MG.mp3"
    val path1 by lazy {
        File(getExternalFilesDir(null), "dem1.mp3").absolutePath
    }
    val path2 by lazy {
        File(getExternalFilesDir(null), "dem2.mp3").absolutePath
    }

    private fun getFileInfo() {
        DownloadManager.downloadFiles(
            this,
            listOf(url, "https://file-examples.com/wp-content/uploads/2017/11/file_example_MP3_1MG.mp3"),
            listOf(path1, path2),
            {
                Log.d(TAG, "OnProgress : $it")
                progress_bar.progress = it.toInt()
                progress_value.text = it.toString()
            },
            {
                Log.d(TAG, "OnComplete")
            },
            {
                Log.d(TAG, "OnFailed : $it")
            })
    }


}
