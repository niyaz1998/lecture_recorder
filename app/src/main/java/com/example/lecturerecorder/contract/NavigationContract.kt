package com.example.lecturerecorder.contract

import com.example.lecturerecorder.model.LectureResponse

interface NavigationContract {
    interface Container {
        fun setActionBarText(text: String)
        fun goToPreviewView(
            lectureId: Int,
            lecture: LectureResponse
        )
        fun goToRecorderView(courseId: Int)
    }

    interface Fragment {

    }
}