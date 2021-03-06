package com.example.lecturerecorder.model

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
    // @FormUrlEncoded
    @POST("api/v1/lectures/")
    fun createLecture(
        @Part("course_id") courseId: RequestBody,
        @Part("name") name: RequestBody,
        // @Part("email")  mEmail: RequestBody,
        // @PartMap params: Map<String, RequestBody>,
        @Part file: MultipartBody.Part?
    ): Single<LectureResponse>
//
//    @POST("api/v1/lectures/{lectureId}")
//    fun putLecture(@Query("lecture_id") lectureId: Int, @Multipart("") course: LecturePost)

    @DELETE("api/v1/lectures/{lecture_id}")
    fun deleteLecture(@Path("lecture_id") lectureId: Int): Completable

    // NOTES #########################################################################

    @GET("api/v1/lectures/{lectureId}/notes")
    fun getNotes(@Path("lectureId") lectureId: Int): Single<List<NoteResponse>?>

    //fun getNote()

    @POST("api/v1/lectures/{lectureId}/notes")
    fun createNote(@Path("lectureId") lectureId: Int, @Body note: NotePost): Single<NoteResponse>

    @POST("api/v1/notes/{noteId}")
    fun putNote(@Path("noteId") noteId: Int, @Body note: NoteResponse) : Single<NoteResponse>

    @DELETE("api/v1/lectures/{lectureId}/notes")
    fun deleteNote(@Path("lectureId") lectureId: Int, @Path("noteId") noteId: Int): Completable

    // FILES #########################################################################

//    @GET("api/v1/files/{fileName}")
//    fun getFile(@Path("fileName") fileName: String): Filedata // Probably should be handled directly, not via retrofit


    // SUBSCRIPTIONS

    @POST("api/v1/subscribes")
    fun subscribeTopic(@Query("topic_id") topicId: Int): Completable

    @POST("api/v1/subscribes")
    fun subscribeCourse(@Query("course_id") courseId: Int): Completable

    @DELETE("api/v1/subscribes")
    fun unsubTopic(@Query("topic_id") topicId: Int): Completable

    @DELETE("api/v1/subscribes")
    fun unsubCourse(@Query("course_id") courseId: Int): Completable

    @GET("api/v1/subscribes")
    fun getSubs(): Single<SubResponse>

}