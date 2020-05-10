package com.example.lecturerecorder.model

import com.google.gson.annotations.SerializedName

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