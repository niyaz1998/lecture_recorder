package com.example.lecturerecorder.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lecturerecorder.model.LectureResponse
import com.example.lecturerecorder.model.ListElement

class ListViewModel: ViewModel() {
    val topics = MutableLiveData<List<ListElement>>(emptyList())
    val courses = MutableLiveData<List<ListElement>>(emptyList())
    val lectures = MutableLiveData<List<ListElement>>(emptyList())
    val subscriptions = MutableLiveData<List<ListElement>>(emptyList())

    /*
    чтобы запомнить текущие лекции и потом открыть сцену с превью лекции
     */
    val lectureModels = MutableLiveData<List<LectureResponse>>(emptyList())

    val selectedTopicId = MutableLiveData<Int?>(null)
    val selectedTopicName = MutableLiveData<String>("")
    val isTopicOwned = MutableLiveData<Boolean>(true)

    val selectedCourseId = MutableLiveData<Int?>(null)
    val selectedCourseName = MutableLiveData<String>("")
    val isCourseOwned = MutableLiveData<Boolean>(true)
}