package com.example.lecturerecorder.model

enum class ListElementType {
    Detailed,
    Short
}

data class ListElement(val type: ListElementType, val title: String, val description: String?, val info: String?, val id: String)
