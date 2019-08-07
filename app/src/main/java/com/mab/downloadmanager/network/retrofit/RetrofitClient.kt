package com.mab.downloadmanager.network.retrofit

import com.mab.downloadmanager.network.OnDownloadUpdateListener
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * Created by Mirza Ahmed Baig on 2019-08-05.
 * Avantari Technologies
 * mirza@avantari.org
 */
object RetrofitClient {

    fun getDownloadRetrofit(listener: OnDownloadUpdateListener): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.43.135/retro/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpDownloadClientBuilder(listener).build())
            .build()
    }

    private fun getOkHttpDownloadClientBuilder(progressListener: OnDownloadUpdateListener?): OkHttpClient.Builder {
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.connectTimeout(20, TimeUnit.SECONDS)
        httpClientBuilder.writeTimeout(0, TimeUnit.SECONDS)
        httpClientBuilder.readTimeout(5, TimeUnit.MINUTES)

        httpClientBuilder.addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                if (progressListener == null) return chain.proceed(chain.request())

                var validListener = progressListener
                val originalResponse = chain.proceed(chain.request())
                if (!originalResponse.isSuccessful) {
                    progressListener.onFileDownloadedError("Failed with error : ${originalResponse.code()}")
                    validListener = null
                }
                return originalResponse.newBuilder()
                    .body(
                        ProgressResponseBody(
                            originalResponse.body()!!,
                            validListener
                        )
                    )
                    .build()
            }
        })
        return httpClientBuilder
    }
}