package com.mab.downloadmanager.network.retrofit

import com.mab.downloadmanager.network.OnDownloadUpdateListener
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException


/**
 * Created by Mirza Ahmed Baig on 2019-08-05.
 * Avantari Technologies
 * mirza@avantari.org
 */
class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val progressListener: OnDownloadUpdateListener?
) : ResponseBody() {
    private var bufferedSource: BufferedSource? = null

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource? {
        bufferedSource = bufferedSource ?: Okio.buffer(source(responseBody.source()))
        return bufferedSource
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            var lastProgress = 0f
            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                try {
                    val bytesRead = super.read(sink, byteCount)

                    totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                    val percent =
                        if (bytesRead == -1L) 100f else totalBytesRead.toFloat() / responseBody.contentLength().toFloat() * 100
                    if (lastProgress != percent) {
                        progressListener?.onFileDownloadUpdate(percent, responseBody.contentLength())
                    }
                    lastProgress = percent

                    return bytesRead
                } catch (e: IOException) {
                    progressListener?.onFileDownloadedError(e.message)
                    return 0L
                }

            }
        }
    }
}