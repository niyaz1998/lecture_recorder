package com.example.lecturerecorder.utils

import com.example.lecturerecorder.BuildConfig
import com.example.lecturerecorder.model.AuthService
import com.example.lecturerecorder.model.ListService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RestClient {

    private val okHttpBuilder: OkHttpClient.Builder = OkHttpClient.Builder().apply {
        connectTimeout(10, TimeUnit.SECONDS)
        readTimeout(10, TimeUnit.SECONDS)
        writeTimeout(10, TimeUnit.SECONDS) // TODO restore 1 minute timer
        followRedirects(true)
        followSslRedirects(true)
        addInterceptor {
            it.proceed(
                it.request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer " + getAuthToken())
                    .build()
            )
        }
        addInterceptor {
            var request: Request =
                it.request() // Very Terrible Workaround, But OkHttp Won't support it natively
            var response: Response = it.proceed(it.request())
            if (response.code() == 307 || response.code() == 308) {
                request = request.newBuilder()
                    .url(BuildConfig.BASE_URL.dropLast(1) + response.header("Location"))
                    .build()
                response = it.proceed(request)
            }
            response
        }
    }

    private val retrofit = Retrofit.Builder().apply {
        baseUrl(BuildConfig.BASE_URL)
        client(okHttpBuilder.build())
        addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create()))
        addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    }.build()

    var listService: ListService
    var authService: AuthService

    init {
        listService = retrofit.create(ListService::class.java)
        authService = retrofit.create(AuthService::class.java)
    }
}