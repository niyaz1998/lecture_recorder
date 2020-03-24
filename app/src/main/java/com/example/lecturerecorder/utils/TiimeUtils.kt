package com.example.lecturerecorder.utils

fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val hours = minutes / 60

    return "${formatForTime(hours)}:${formatForTime(minutes)}:${formatForTime(seconds)}"
}

private fun formatForTime(value: Long): String {
    return if (value < 10) "0${value}" else value.toString()
}