package com.example.lecturerecorder.utils

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val hours = minutes / 60

    return "${formatForTime(hours)}:${formatForTime(minutes)}:${formatForTime(seconds % 60)}"
}

private fun formatForTime(value: Int): String {
    return if (value < 10) "0${value}" else value.toString()
}