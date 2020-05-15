package com.example.lecturerecorder.model

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {

    @POST("api/v1/users")
    fun register(@Body credentials: AuthCredentials): Single<RegisterResponse>

    @POST("api/v1/auth/login")
    fun login(@Body credentials: AuthCredentials): Single<LoginResponse>

    @GET("api/v1/auth/refresh_token")
    fun refreshToken(): Single<TokenOnly>
}