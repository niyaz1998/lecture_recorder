package com.example.lecturerecorder.model

import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ListService {

//    @GET("api/breed/{breed}/images/random")
//    fun getDogbyBreed(@Path("breed") breed: String): Call<Dog>

    @GET("api/topics")
    fun getTopics(): Single<List<Topic>>

}