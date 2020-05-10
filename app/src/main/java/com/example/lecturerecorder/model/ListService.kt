package com.example.lecturerecorder.model

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ListService {

//    @GET("api/breed/{breed}/images/random")
//    fun getDogbyBreed(@Path("breed") breed: String): Call<Dog>

    @GET("api/v1/topics")
    fun getTopics(): Single<List<TopicResponse>?>

    @POST("api/v1/topics")
    fun createTopic(@Body topic: TopicPost)

    //fun putTopic()

    //fun deleteTopic()

    @GET("api/v1/topics/{topicId}/courses")
    fun getCourses(@Path("topicId") topicId: Int): Single<List<CourseResponse>?>

    @POST("api/v1/topics/{topicId}/courses")
    fun createCourse(@Path("topicId") topicId: Int, course: CoursePost)

    //fun putCourse

    //fun deleteCourse

//    @GET("api/v1/topics/{topicId}/")
//    fun getLectures(@Path("topicId") topicId: Int): Single<List<CourseResponse>?>
//
//    @POST("api/v1/topics/{topicId}/")
//    fun createLecture(@Path("topicId") topicId: Int, course: CoursePost)

    //fun putLecture

    //fun deleteLecture

}