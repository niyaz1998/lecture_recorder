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
    val id: String
)

data class TopicPost(
    @SerializedName("Name")
    val name: String,
    @SerializedName("Description")
    val description: String
)

data class TopicResponse(
    @SerializedName("ID")
    val id: Int,
    @SerializedName("Name")
    val name: String,
    @SerializedName("Description")
    val description: String,
    @SerializedName("Courses")
    val courses: Int
)