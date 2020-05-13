package com.example.lecturerecorder.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Note(var text: String, var seconds: Long) : Parcelable