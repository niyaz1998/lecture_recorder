package com.example.lecturerecorder.model

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ListService {

//    @GET("api/breed/{breed}/images/random")
//    fun getDogbyBreed(@Path("breed") breed: String): Call<Dog>

    @GET("api/v1/topics")
    fun getTopics(): Single<List<TopicResponse>>

    @POST("api/v1/topics")
    fun createTopic(@Body topic: TopicPost)

    //fun selfInfo(): Single<>

}