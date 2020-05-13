package com.example.lecturerecorder.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LectureRecord(
    var name: String,
    var notes: List<Note>,
    var fileLocation: String
) : Parcelable