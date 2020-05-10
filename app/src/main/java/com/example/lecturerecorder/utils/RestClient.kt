package com.example.lecturerecorder.utils

import com.example.lecturerecorder.BuildConfig
import com.example.lecturerecorder.model.AuthService
import com.example.lecturerecorder.model.ListService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RestClient {

    private val okHttpBuilder: OkHttpClient.Builder = OkHttpClient.Builder().apply {
        connectTimeout(10, TimeUnit.SECONDS)
        readTimeout(10, TimeUnit.SECONDS)
        writeTimeout(10, TimeUnit.SECONDS) // TODO restore 1 minute timer
        addInterceptor{
            it.proceed(
                it.request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer " + getAuthToken())
                    .build()
            )
        }
    }

    private val retrofit = Retrofit.Builder().apply {
        baseUrl(BuildConfig.BASE_URL)
        client(okHttpBuilder.build())
        addConverterFactory(GsonConverterFactory.create())
        addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    }.build()

    var listService: ListService
    var authService: AuthService

    init {
        listService = retrofit.create(ListService::class.java)
        authService = retrofit.create(AuthService::class.java)
    }
}