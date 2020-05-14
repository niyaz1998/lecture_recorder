package com.example.lecturerecorder.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lecturerecorder.model.LectureResponse
import com.example.lecturerecorder.model.ListElement

class ListViewModel: ViewModel() {
    val topics = MutableLiveData<List<ListElement>>(emptyList())
    val courses = MutableLiveData<List<ListElement>>(emptyList())
    val lectures = MutableLiveData<List<ListElement>>(emptyList())

    /*
    чтобы запомнить текущие лекции и потом открыыть сцену с превью лекции
     */
    val lectureModels = MutableLiveData<List<LectureResponse>>(emptyList())

    val selectedTopicId = MutableLiveData<Int?>(null)
    val selectedCourseId = MutableLiveData<Int?>(null)
}