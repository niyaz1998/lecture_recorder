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
        fun setHeaderTitle(text: String)
        fun setHeaderVisibility(state: Boolean)
        fun resetNavigation()
    }

    interface Fragment {
        fun subscribeClicked()
        fun navigateToAll()
        fun navigateToPersonal()
        fun navigateToSubscriptions()
    }
}