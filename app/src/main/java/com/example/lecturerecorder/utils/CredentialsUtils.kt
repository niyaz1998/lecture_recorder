package com.example.lecturerecorder.utils

import android.content.Context
import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.ErrorResponse
import com.google.gson.Gson
import retrofit2.HttpException

fun storeAuthToken(token: String) {
    val context = LectureRecorder.appContext
    val sharedPref = context?.getSharedPreferences(context.getString(R.string.APPLICATION_SHARED_PREF), Context.MODE_PRIVATE) ?: return
    with (sharedPref.edit()) {
        putString(context.getString(R.string.AUTH_TOKEN), token)
        commit()
    }
}

fun getAuthToken(): String {
    val context = LectureRecorder.appContext
    val sharedPref = context?.getSharedPreferences(context.getString(R.string.APPLICATION_SHARED_PREF), Context.MODE_PRIVATE) ?: return ""
    return sharedPref.getString(context.getString(R.string.AUTH_TOKEN), "") ?: return ""
}

fun parseHttpErrorMessage(error: Throwable): String {
    var message = error.localizedMessage
    if (error is HttpException) {
        val body = error.response().errorBody()
        val gson = Gson()
        val adapter = gson.getAdapter<ErrorResponse>(ErrorResponse::class.java)
        try {
            val http_error = adapter.fromJson(body?.string())
            if (!http_error.message.isNullOrBlank()) {
                message = http_error.message
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return message ?: return "Error Occurred"
}