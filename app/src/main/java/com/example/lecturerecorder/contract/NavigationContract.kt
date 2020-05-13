package com.example.lecturerecorder.contract

interface NavigationContract {
    interface Container {
        fun setActionBarText(text: String)
        fun goToPreviewView(lectureId: Int)
        fun goToRecorderView(courseId: Int)
    }

    interface Fragment {

    }
}