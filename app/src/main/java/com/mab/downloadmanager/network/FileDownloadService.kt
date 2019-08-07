package com.mab.downloadmanager.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url


/**
 * Created by Mirza Ahmed Baig on 2019-08-05.
 * Avantari Technologies
 * mirza@avantari.org
 */
interface FileDownloadService {
    @GET
    fun downloadFileWithDynamicUrlSync(@Url fileUrl: String): Call<ResponseBody>
}