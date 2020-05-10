package com.example.lecturerecorder.utils

import android.app.Activity
import android.content.Context
import android.provider.Settings.Global.getString
import android.view.View
import com.example.lecturerecorder.R
import com.example.lecturerecorder.model.ErrorResponse
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.HttpException
import java.lang.Exception

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
            val error = adapter.fromJson(body?.string())
            if (!error.message.isNullOrBlank()) {
                message = error.message
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return message ?: return "Error Occurred"
}