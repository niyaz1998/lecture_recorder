package com.example.lecturerecorder.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

// UI ELEMENTS #############################################

enum class ListElementType {
    Detailed,
    Short
}

data class ListElement(
    val type: ListElementType,
    val title: String,
    val description: String?,
    val info: String?,
    val id: Int
)

// TOPICS #############################################

data class TopicPost(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String
)

data class TopicResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("courses")
    val courses: Int
)

// COURSES #############################################

data class CoursePost(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String
)

data class CourseResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("audios")
    val audios: Int,
    @SerializedName("topic")
    val topic: Int
)

// LECTURES #############################################

data class LecturePost(
    @SerializedName("name")
    val name: String,
    @SerializedName("course_id")
    val courseId: Int
)

data class LecturePut(
    @SerializedName("name")
    val name: String
)

@Parcelize
data class LectureResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("audio_file")
    val audioFile: String,
    @SerializedName("course_id")
    val courseId:  Int,
    @SerializedName("id")
    val id:  Int,
    @SerializedName("notes")
    val note: List<NoteResponse>?
) : Parcelable

@Parcelize
data class NoteResponse(
    @SerializedName("lecture_id")
    val lectureId: Int,
    @SerializedName("text")
    var text: String,
    @SerializedName("timestamp")
    val timestamp: Int, // in seconds
    @SerializedName("id")
    val id:  Int,
    @SerializedName("picture")
    val picture: String
) : Parcelable

data class SubResponse (
    @SerializedName("topics")
    val topics: List<TopicResponse>,
    @SerializedName("courses")
    val courses: List<CourseResponse>
)

data class NotePost(
    @SerializedName("lecture_id")
    val lectureId: Int,
    @SerializedName("text")
    var text: String,
    @SerializedName("timestamp")
    val timestamp: Int, // in seconds
    @SerializedName("picture")
    val picture: String?
)

