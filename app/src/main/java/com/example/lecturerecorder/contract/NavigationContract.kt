package com.example.lecturerecorder.contract

interface NavigationContract {
    interface Container {
        fun setActionBarText(text: String)
        fun goToPreviewView(lectureId: Int)
        fun goToRecorderView(courseId: Int)
        fun setHeaderTitle(text: String)
        fun setHeaderVisibility(state: Boolean)
    }

    interface Fragment {
        fun subscribeClicked()
        fun navigateToAll()
        fun navigateToPersonal()
        fun navigateToSubscriptions()
    }
}