package com.example.lecturerecorder.model

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.*

interface ListService {

    // TOPICS ################################################################

    @GET("api/v1/topics")
    fun getTopics(): Single<List<TopicResponse>?>

    @POST("api/v1/topics")
    fun createTopic(@Body topic: TopicPost): Single<TopicResponse>

    @PUT("api/v1/topics/{topicId}")
    fun putTopic(@Path("topicId") topicId: Int, @Body topic: TopicPost): Single<TopicResponse>

    @DELETE("api/v1/topics/{topicId}")
    fun deleteTopic(@Path("topicId") topicId: Int): Completable

    // COURSES ################################################################

    @GET("api/v1/topics/{topicId}/courses")
    fun getCourses(@Path("topicId") topicId: Int): Single<List<CourseResponse>?>

    @POST("api/v1/topics/{topicId}/courses")
    fun createCourse(
        @Path("topicId") topicId: Int,
        @Body course: CoursePost
    ): Single<CourseResponse>

    @PUT("api/v1/topics/{topicId}/courses/{courseId}")
    fun putCourse(
        @Path("topicId") topicId: Int, @Path("courseId") courseId: Int,
        @Body course: CoursePost
    ): Single<CourseResponse>

    @DELETE("api/v1/topics/{topicId}/courses/{courseId}")
    fun deleteCourse(@Path("topicId") topicId: Int, @Path("courseId") courseId: Int): Completable

    // LECTURES ################################################################

    @GET("api/v1/lectures")
    fun getLectures(@Query("course_id") courseId: Int): Single<List<LectureResponse>?>

    @Multipart
    @POST("api/v1/lectures")
    fun createLecture(
        @Query("course_id") courseId: Int,
        @Query("name") name: String,
        @Part file: MultipartBody.Part?
    ) : Single<LectureResponse>
//
//    @POST("api/v1/lectures/{lectureId}")
//    fun putLecture(@Query("lecture_id") lectureId: Int, @Multipart("") course: LecturePost)

    @POST("api/v1/lectures/{lectureid}")
    fun deleteLecture(@Query("lecture_id") lectureId: Int): Completable

    // NOTES #########################################################################

    @GET("api/v1/lectures/{lectureId}/notes")
    fun getNotes(@Path("lectureId") lectureId: Int): Single<List<NoteResponse>?>

    //fun getNote()

    @POST("api/v1/lectures/{lectureId}/notes")
    fun createNote(@Path("lectureId") lectureId: Int): Single<NoteResponse>

    //fun putNote()

    @DELETE("api/v1/lectures/{lectureId}/notes")
    fun deleteNote(@Path("lectureId") lectureId: Int, @Path("noteId") noteId: Int): Completable

    // FILES #########################################################################

//    @GET("api/v1/files/{fileName}")
//    fun getFile(@Path("fileName") fileName: String): Filedata // Probably should be handled directly, not via retrofit

}