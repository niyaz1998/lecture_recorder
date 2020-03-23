package com.example.lecturerecorder.recorder_activity

import android.media.MediaRecorder
import android.util.Log
import java.io.IOException
import java.util.*


enum class RecorderState { NOT_STARTED, RECORDING, RECORDED }

private const val LOG_TAG = "RecorderViewModel"

class RecorderViewModel(private val fileName: String, private val view: RecorderActivity) {
    private var recorder: MediaRecorder? = null
    private var state: RecorderState = RecorderState.NOT_STARTED
    private var timer: Timer? = null

    init {
        view.setButton(ButtonState.RECORD)
    }

    fun onMainButtonPressed() {
        if (state == RecorderState.NOT_STARTED) {
            startRecording()
            startTimer()
            state = RecorderState.RECORDING
            view.setButton(ButtonState.STOP_RECORD)
        } else if (state == RecorderState.RECORDING) {
            stopRecording()
            stopTimer()
            state = RecorderState.RECORDED
            view.setButton(ButtonState.NULL)
        }
    }

    fun onStop() {
        recorder?.stop()
        recorder?.reset()
        recorder?.release()
        recorder = null
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

    private fun startTimer() {
        timer = Timer()
        timer?.schedule(MillisPassedTimer(Calendar.getInstance()) {
            view.setTime(it)
        }, 0, 1000)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
        view.setTime(0)
    }
}

class MillisPassedTimer(
    private val time: Calendar,
    private val callback: (Seconds: Long) -> Unit
) :
    TimerTask() {

    override fun run() {
        callback((Calendar.getInstance().timeInMillis - time.timeInMillis))
    }
}