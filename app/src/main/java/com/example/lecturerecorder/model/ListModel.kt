package com.example.lecturerecorder.model

import com.google.gson.annotations.SerializedName

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

data class LectureResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("audio_file")
    val audioFile: String,
    @SerializedName("course_id")
    val courseId:  Int,
    @SerializedName("notes")
    val note: List<NoteResponse>
)

data class NoteResponse( // FIXME: already implemented in Note.kt
    @SerializedName("lecture_id")
    val lectureId: Int,
    @SerializedName("text")
    val text: String,
    @SerializedName("picture")
    val picture: String
)