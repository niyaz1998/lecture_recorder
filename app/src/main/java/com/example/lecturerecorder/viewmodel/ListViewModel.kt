package com.example.lecturerecorder.viewmodel

import androidx.lifecycle.MutableLiveData
import com.example.lecturerecorder.model.ListElement

class ListViewModel {
    private val topics = MutableLiveData<List<ListElement>>(emptyList())
    private val courses = MutableLiveData<List<ListElement>>(emptyList())
    private val lectures = MutableLiveData<List<ListElement>>(emptyList())
}