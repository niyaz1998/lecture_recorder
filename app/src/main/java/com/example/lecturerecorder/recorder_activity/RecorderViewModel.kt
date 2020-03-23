package com.example.lecturerecorder.recorder_activity

import android.media.MediaRecorder
import android.util.Log
import java.io.IOException


enum class RecorderState { NOT_STARTED, RECORDING, RECORDED }

private const val LOG_TAG = "RecorderViewModel"

class RecorderViewModel(private val fileName: String) {
    private var recorder: MediaRecorder? = null
    private var state: RecorderState = RecorderState.NOT_STARTED

    fun onMainButtonPressed() {
        if (state == RecorderState.NOT_STARTED) {
            startRecording()
            state = RecorderState.RECORDING
        } else if (state == RecorderState.RECORDING) {
            stopRecording()
            state = RecorderState.RECORDED
        }
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            reset()
            release()
        }
        recorder = null
    }

    fun onStop() {
        recorder?.stop()
        recorder?.reset()
        recorder?.release()
        recorder = null
    }
}